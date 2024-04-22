package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.helper.repository.searcher.ServiceQuadSearcherForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.SearchUtilForTest.callAndAssertSearch;
import static com.eskgus.nammunity.util.SearchUtilForTest.initializeSearchHelper;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsSearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostsSearchService postsSearchService;

    private User[] users;
    private final int page = 1;

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
    public void findAllDesc() {
        savePosts();

        callAndAssertFindAllDesc();
    }

    private void savePosts() {
        int numberOfPostsByUser = 15;
        for (int i = 0; i < numberOfPostsByUser; i++) {
            for (User user : users) {
                testDB.savePosts(user);
            }
        }
        assertThat(postsRepository.count()).isEqualTo((long) numberOfPostsByUser * users.length);
    }

    private void callAndAssertFindAllDesc() {
        ContentsPageDto<PostsListDto> actualResult = postsSearchService.findAllDesc(page);
        Page<PostsListDto> expectedContents = createExpectedContents();

        ContentsPageDtoTestHelper<PostsListDto, Posts> findHelper = ContentsPageDtoTestHelper.<PostsListDto, Posts>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new PostsConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private Page<PostsListDto> createExpectedContents() {
        Pageable pageable = createPageable(page, 20);
        return postsRepository.findAllDesc(pageable);
    }

    @Test
    public void findByUser() {
        savePosts();

        callAndAssertFindByUser();
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Page<PostsListDto> actualPage = postsSearchService.findByUser(user, page, size);
        Page<PostsListDto> expectedPage = createExpectedPage(size, user);

        assertFindByUser(actualPage, expectedPage);
    }

    private Page<PostsListDto> createExpectedPage(int size, User user) {
        Pageable pageable = createPageable(page, size);
        return postsRepository.findByUser(user, pageable);
    }

    private void assertFindByUser(Page<PostsListDto> actualPage, Page<PostsListDto> expectedPage) {
        PaginationTestHelper<PostsListDto, Posts> paginationHelper
                = new PaginationTestHelper<>(actualPage, expectedPage, new PostsConverterForTest());
        paginationHelper.assertContents();
    }

    @Test
    public void search() {
        savePostsWithTitleAndContent();

        // 1. 제목 + 내용
        callAndAssertSearchPosts("흥 100 Let", SearchType.TITLE_AND_CONTENT.getKey()
                , Posts::getTitle, Posts::getContent);

        // 2. 제목
        callAndAssertSearchPosts("흥 100 Let", SearchType.TITLE.getKey(), Posts::getTitle);

        // 3. 내용
        callAndAssertSearchPosts("흥 100 Let -봉,마리", SearchType.CONTENT.getKey(), Posts::getContent);
    }

    private void savePostsWithTitleAndContent() {
        String str1 = "default";
        String str2 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str3 = "붕어빵 3마리 1000원";
        String[] strings = { str1, str2, str3 };
        for (User user : users) {
            testDB.savePosts(user, strings);
        }
        assertThat(postsRepository.count()).isEqualTo((long) Math.pow(strings.length, 2) * users.length);
    }

    private void callAndAssertSearchPosts(String keywords, String searchBy, Function<Posts, String>... fieldExtractors) {
        SearchHelperForTest<ServiceQuadSearcherForTest<PostsListDto>, Posts, PostsListDto> searchHelper
                = createSearchHelper(postsSearchService::search, keywords, searchBy, fieldExtractors);
        initializeSearchHelper(searchHelper);
        callAndAssertSearch();
    }

    private SearchHelperForTest<ServiceQuadSearcherForTest<PostsListDto>, Posts, PostsListDto>
        createSearchHelper(ServiceQuadSearcherForTest<PostsListDto> searcher,
                           String keywords, String searchBy, Function<Posts, String>... fieldExtractors) {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return SearchHelperForTest.<ServiceQuadSearcherForTest<PostsListDto>, Posts, PostsListDto>builder()
                .searcher(searcher).keywords(keywords)
                .totalContents(postsRepository.findAll())
                .searchBy(searchBy).fieldExtractors(fieldExtractors)
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }
}
