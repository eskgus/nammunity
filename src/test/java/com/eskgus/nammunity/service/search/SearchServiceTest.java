package com.eskgus.nammunity.service.search;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.ContentsPageMoreDtoTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.function.Function;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private PostsService postsService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private UserService userService;

    @Autowired
    private SearchService searchService;

    private final int page = 1;

    @BeforeEach
    public void setUp() {
        saveContents();
    }

    private void saveContents() {
        long numberOfContents = 12;
        long half = numberOfContents / 2;

        Range firstRange = Range.builder()
                .startIndex(1).endIndex(half)
                .nickname("user").title("title").content("content").comment("comment").build();
        Range secondRange = Range.builder()
                .startIndex(half + 1).endIndex(numberOfContents)
                .nickname("사용자").title("제목").content("내용").comment("댓글").build();

        saveContentsInRange(firstRange);
        saveContentsInRange(secondRange);
    }

    private void saveContentsInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long userId = testDB.signUp(range.getNickname() + i, i, Role.USER);
            User user = assertOptionalAndGetEntity(userRepository::findById, userId);

            Long postId = testDB.savePostWithTitleAndContent(user, range.getTitle() + i, range.getContent() + i);
            assertOptionalAndGetEntity(postsRepository::findById, postId);

            Long commentId = testDB.saveCommentWithContent(postId, user, range.getComment() + i);
            assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        }
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void search() {
        callAndAssertSearch();
    }

    private void callAndAssertSearch() {
        String keywords = "e";  // user, title, content, comment에서 겹치는 글자 e
        int size = 5;

        ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> actualResult = searchService.search(keywords);

        Page<PostsListDto> postsPage = postsService.search(keywords, SearchType.TITLE_AND_CONTENT.getKey(), page, size);
        Page<CommentsListDto> commentsPage = commentsService.searchByContent(keywords, page, size);
        Page<UsersListDto> usersPage = userService.searchByNickname(keywords, page, size);

        ContentsPageMoreDtoTestHelper<PostsListDto, CommentsListDto, UsersListDto> findHelper
                = new ContentsPageMoreDtoTestHelper<>(actualResult, postsPage, commentsPage, usersPage);
        findHelper.createExpectedResultAndAssertContentsPageMore();
    }

    @Test
    public void searchPosts() {
        // 1. searchBy = title (title)
        callAndAssertSearchPosts("title", SearchType.TITLE);

        // 2. searchBy = content (content)
        callAndAssertSearchPosts("content", SearchType.CONTENT);

        // 3. searchBy = title and content (제목 내용)
        callAndAssertSearchPosts("제목 내용", SearchType.TITLE_AND_CONTENT);
    }

    private void callAndAssertSearchPosts(String keywords, SearchType searchType) {
        String searchBy = searchType.getKey();
        ContentsPageDto<PostsListDto> actualResult = searchService.searchPosts(keywords, searchBy, page);
        Page<PostsListDto> expectedContents = postsService.search(keywords, searchBy, page, 30);

        ContentsPageDtoTestHelper<PostsListDto, Posts> findHelper = ContentsPageDtoTestHelper.<PostsListDto, Posts>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new PostsConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    @Test
    public void searchComments() {
        String keywords = "comment";
        ContentsPageDto<CommentsListDto> actualResult = searchService.searchComments(keywords, page);
        Page<CommentsListDto> expectedContents = commentsService.searchByContent(keywords, page, 30);

        ContentsPageDtoTestHelper<CommentsListDto, Comments> findHelper = ContentsPageDtoTestHelper.<CommentsListDto, Comments>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new CommentsConverterForTest<>(CommentsListDto.class)).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    @Test
    public void searchUsers() {
        String keywords = "user";
        ContentsPageDto<UsersListDto> actualResult = searchService.searchUsers(keywords, page);
        Page<UsersListDto> expectedContents = userService.searchByNickname(keywords, page, 30);

        ContentsPageDtoTestHelper<UsersListDto, User> findHelper = ContentsPageDtoTestHelper.<UsersListDto, User>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new UserConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }
}
