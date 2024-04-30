package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.helper.repository.finder.RepositoryBiFinderForTest;
import com.eskgus.nammunity.helper.repository.searcher.RepositoryBiSearcherForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;
import static com.eskgus.nammunity.util.SearchUtilForTest.callAndAssertSearch;
import static com.eskgus.nammunity.util.SearchUtilForTest.initializeSearchHelper;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User[] users;
    private Posts post;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. 댓글 작성 x 후 호출
        callAndAssertCountByUser(0L);

        // 2. 댓글 1개 작성 후 호출
        Comments comment = saveCommentAndGetSavedComment();
        callAndAssertCountByUser(comment.getId());
    }

    private void callAndAssertCountByUser(Long expectedCount) {
        Long actualCount = commentsRepository.countByUser(users[0]);
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    private Comments saveCommentAndGetSavedComment() {
        Long commentId = testDB.saveComments(post.getId(), users[0]);
        return getSavedComment(commentId);
    }

    private Comments getSavedComment(Long commentId) {
        Optional<Comments> result = commentsRepository.findById(commentId);
        assertThat(result).isPresent();
        return result.get();
    }

    @Test
    public void searchByContent() {
        saveComments();

        // 1. 검색 제외 단어 x
        callAndAssertSearchCommentsByFields(commentsRepository::searchByContent, "흥 100 Let", Comments::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearchCommentsByFields(commentsRepository::searchByContent, "흥 100 Let -봉,마리", Comments::getContent);
    }

    private void saveComments() {
        String str1 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str2 = "붕어빵 3마리 1000원";
        String[] strings = { str1, str2 };
        for (User user : users) {
            testDB.saveCommentsWithContent(post.getId(), user, strings);
        }
        assertThat(commentsRepository.count()).isEqualTo(strings.length * users.length);
    }

    private void callAndAssertSearchCommentsByFields(RepositoryBiSearcherForTest<CommentsListDto> searcher,
                                                     String keywords, Function<Comments, String>... fieldExtractors) {
        SearchHelperForTest<RepositoryBiSearcherForTest<CommentsListDto>, Comments, CommentsListDto> searchHelper
                = createSearchHelper(searcher, keywords, fieldExtractors);
        initializeSearchHelper(searchHelper);
        callAndAssertSearch();
    }

    private SearchHelperForTest<RepositoryBiSearcherForTest<CommentsListDto>, Comments, CommentsListDto>
        createSearchHelper(RepositoryBiSearcherForTest<CommentsListDto> searcher,
                           String keywords, Function<Comments, String>... fieldExtractors) {
        EntityConverterForTest<Comments, CommentsListDto> entityConverter
                = new CommentsConverterForTest(CommentsListDto.class);
        return SearchHelperForTest.<RepositoryBiSearcherForTest<CommentsListDto>, Comments, CommentsListDto>builder()
                .searcher(searcher).keywords(keywords)
                .totalContents(commentsRepository.findAll())
                .fieldExtractors(fieldExtractors)
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }

    @Test
    public void findByUser() {
        saveComments();

        FindHelperForTest<RepositoryBiFinderForTest<CommentsListDto, User>, Comments, CommentsListDto, User> findHelper
                = createBiFindHelperWithUser();
        callAndAssertFindComments(findHelper);
    }

    private FindHelperForTest<RepositoryBiFinderForTest<CommentsListDto, User>, Comments, CommentsListDto, User>
        createBiFindHelperWithUser() {
        EntityConverterForTest<Comments, CommentsListDto> entityConverter
                = new CommentsConverterForTest(CommentsListDto.class);
        return FindHelperForTest.<RepositoryBiFinderForTest<CommentsListDto, User>, Comments, CommentsListDto, User>builder()
                .finder(commentsRepository::findByUser)
                .contents(users[0])
                .entityStream(commentsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }

    private <V, W> void callAndAssertFindComments(FindHelperForTest<RepositoryBiFinderForTest<V, W>, Comments, V, W>
                                                          findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    @Test
    public void findByPosts() {
        saveComments();

        FindHelperForTest<RepositoryBiFinderForTest<CommentsReadDto, Posts>, Comments, CommentsReadDto, Posts>
                findHelper = createBiFindHelperWithPosts();
        callAndAssertFindComments(findHelper);
    }

    private FindHelperForTest<RepositoryBiFinderForTest<CommentsReadDto, Posts>, Comments, CommentsReadDto, Posts>
        createBiFindHelperWithPosts() {
        EntityConverterForTest<Comments, CommentsReadDto> entityConverter
                = new CommentsConverterForTest(CommentsReadDto.class);
        return FindHelperForTest.<RepositoryBiFinderForTest<CommentsReadDto, Posts>, Comments, CommentsReadDto, Posts>builder()
                .finder(commentsRepository::findByPosts)
                .contents(post)
                .entityStream(commentsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }
}
