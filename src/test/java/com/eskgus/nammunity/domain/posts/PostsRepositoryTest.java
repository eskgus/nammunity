package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.helper.repository.finder.RepositoryBiFinderForTest;
import com.eskgus.nammunity.helper.repository.finder.RepositoryFinderForTest;
import com.eskgus.nammunity.helper.repository.searcher.RepositoryBiSearcherForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.util.SearchUtilForTest.callAndAssertSearch;
import static com.eskgus.nammunity.util.SearchUtilForTest.initializeSearchHelper;
import static org.assertj.core.api.Assertions.assertThat;
import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User[] users;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void save() {
        Posts post = createPost("title", "content");
        callAndAssertSavePosts(post);
    }

    private Posts createPost(String title, String content) {
        return Posts.builder().title(title).content(content).user(users[0]).build();
    }

    private void callAndAssertSavePosts(Posts post) {
        postsRepository.save(post);
        Posts actualPost = getSavedPost(post.getId());
        assertActualPostEqualsExpectedPost(actualPost, post);
    }

    private Posts getSavedPost(Long postId) {
        Optional<Posts> result = postsRepository.findById(postId);
        assertThat(result).isPresent();
        return result.get();
    }

    private void assertActualPostEqualsExpectedPost(Posts actualPost, Posts expectedPost) {
        assertThat(actualPost.getTitle()).isEqualTo(expectedPost.getTitle());
        assertThat(actualPost.getContent()).isEqualTo(expectedPost.getContent());
    }

    @Test
    public void addBaseTimeEntity() {
        Posts savedPost = savePostAndGetSavedPost();

        LocalDateTime now = LocalDateTime.now();

        assertThat(savedPost.getCreatedDate()).isBefore(now);
        assertThat(savedPost.getModifiedDate()).isBefore(now);
    }

    private Posts savePostAndGetSavedPost() {
        Long postId = testDB.savePosts(users[0]);
        return getSavedPost(postId);
    }

    @Test
    public void countByUser() {
        // 1. 게시글 작성 x 후 호출
        callAndAssertCountByUser(0L);

        // 2. 게시글 1개 작성 후 호출
        Posts post = savePostAndGetSavedPost();
        callAndAssertCountByUser(post.getId());
    }

    private void callAndAssertCountByUser(Long expectedCount) {
        Long actualCount = postsRepository.countByUser(users[0]);
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Test
    public void searchByTitle() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearchPostsByFields(postsRepository::searchByTitle, "흥 100 Let", Posts::getTitle);

        // 2. 검색 제외 단어 o
        callAndAssertSearchPostsByFields(postsRepository::searchByTitle, "흥 100 Let -봉,마리", Posts::getTitle);
    }

    private void savePosts() {
        String str1 = "default";
        String str2 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str3 = "붕어빵 3마리 1000원";
        String[] strings = { str1, str2, str3 };
        for (User user : users) {
            testDB.savePostsWithTitleAndContent(user, strings);
        }
        assertThat(postsRepository.count()).isEqualTo((long) Math.pow(strings.length, 2) * users.length);
    }

    private void callAndAssertSearchPostsByFields(RepositoryBiSearcherForTest<PostsListDto> searcher,
                                                  String keywords, Function<Posts, String>... fieldExtractors) {
        SearchHelperForTest<RepositoryBiSearcherForTest<PostsListDto>, Posts, PostsListDto> searchHelper
                = createSearchHelper(searcher, keywords, fieldExtractors);
        initializeSearchHelper(searchHelper);
        callAndAssertSearch();
    }

    private SearchHelperForTest<RepositoryBiSearcherForTest<PostsListDto>, Posts, PostsListDto>
        createSearchHelper(RepositoryBiSearcherForTest<PostsListDto> searcher,
                           String keywords, Function<Posts, String>... fieldExtractors) {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return SearchHelperForTest.<RepositoryBiSearcherForTest<PostsListDto>, Posts, PostsListDto>builder()
                .searcher(searcher).keywords(keywords)
                .totalContents(postsRepository.findAll())
                .fieldExtractors(fieldExtractors)
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }

    @Test
    public void searchByContent() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearchPostsByFields(postsRepository::searchByContent, "흥 100 Let", Posts::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearchPostsByFields( postsRepository::searchByContent, "흥 100 Let -봉,마리", Posts::getContent);
    }

    @Test
    public void searchByTitleAndContent() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearchPostsByFields(postsRepository::searchByTitleAndContent, "흥 100 Let",
                Posts::getTitle, Posts::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearchPostsByFields(postsRepository::searchByTitleAndContent, "흥 100 Let -봉,마리",
                Posts::getTitle, Posts::getContent);
    }

    @Test
    public void findAllDesc() {
        savePosts();

        FindHelperForTest<RepositoryFinderForTest<PostsListDto>, Posts, PostsListDto, Void> findHelper = createFindHelper();
        callAndAssertFindPosts(findHelper);
    }

    private FindHelperForTest<RepositoryFinderForTest<PostsListDto>, Posts, PostsListDto, Void> createFindHelper() {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return FindHelperForTest.<RepositoryFinderForTest<PostsListDto>, Posts, PostsListDto, Void>builder()
                .finder(postsRepository::findAllDesc)
                .entityStream(postsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }

    private void callAndAssertFindPosts(FindHelperForTest findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    @Test
    public void findByUser() {
        savePosts();

        FindHelperForTest<RepositoryBiFinderForTest<PostsListDto, User>, Posts, PostsListDto, User> findHelper = createBiFindHelper();
        callAndAssertFindPosts(findHelper);
    }

    private FindHelperForTest<RepositoryBiFinderForTest<PostsListDto, User>, Posts, PostsListDto, User> createBiFindHelper() {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return FindHelperForTest.<RepositoryBiFinderForTest<PostsListDto, User>, Posts, PostsListDto, User>builder()
                .finder(postsRepository::findByUser)
                .contents(users[0])
                .entityStream(postsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }
}
