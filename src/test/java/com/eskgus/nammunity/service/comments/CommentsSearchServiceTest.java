package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.helper.repository.finder.ServiceTriFinderForTest;
import com.eskgus.nammunity.helper.repository.searcher.ServiceTriSearcherForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;
import static com.eskgus.nammunity.util.SearchUtilForTest.callAndAssertSearch;
import static com.eskgus.nammunity.util.SearchUtilForTest.initializeSearchHelper;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsSearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsSearchService commentsSearchService;

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
    public void findByUser() {
        saveComments();

        callAndAssertFindComments();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 15;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                testDB.saveComments(post.getId(), user);
            }
        }
        assertThat(commentsRepository.count()).isEqualTo(numberOfCommentsByUser * users.length);
    }

    private void callAndAssertFindComments() {
        FindHelperForTest<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User> findHelper
                = createTriFindHelper();

        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    private FindHelperForTest<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User>
        createTriFindHelper() {
        EntityConverterForTest<Comments, CommentsListDto> entityConverter
                = new CommentsConverterForTest(CommentsListDto.class);
        return FindHelperForTest.<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User>builder()
                .finder(commentsSearchService::findByUser)
                .contents(users[0])
                .entityStream(commentsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }

    @Test
    public void searchByContent() {
        saveCommentsWithContent();

        // 1. 검색 제외 단어 x
        callAndAssertSearchComments("흥 100 Let", Comments::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearchComments("흥 100 Let -봉,마리", Comments::getContent);
    }

    private void saveCommentsWithContent() {
        String str1 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str2 = "붕어빵 3마리 1000원";
        String[] strings = { str1, str2 };
        for (User user : users) {
            testDB.saveComments(post.getId(), user, strings);
        }
        assertThat(commentsRepository.count()).isEqualTo(strings.length * users.length);
    }

    private void callAndAssertSearchComments(String keywords, Function<Comments, String>... fieldExtractors) {
        SearchHelperForTest<ServiceTriSearcherForTest<CommentsListDto>, Comments, CommentsListDto> searchHelper
                = createSearchHelper(commentsSearchService::searchByContent, keywords, fieldExtractors);
        initializeSearchHelper(searchHelper);
        callAndAssertSearch();
    }

    private SearchHelperForTest<ServiceTriSearcherForTest<CommentsListDto>, Comments, CommentsListDto>
        createSearchHelper(ServiceTriSearcherForTest<CommentsListDto> searcher,
                           String keywords, Function<Comments, String>... fieldExtractors) {
        EntityConverterForTest<Comments, CommentsListDto> entityConverter
                = new CommentsConverterForTest(CommentsListDto.class);
        return SearchHelperForTest.<ServiceTriSearcherForTest<CommentsListDto>, Comments, CommentsListDto>builder()
                .searcher(searcher).keywords(keywords)
                .totalContents(commentsRepository.findAll())
                .fieldExtractors(fieldExtractors)
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }

    @Test
    public void findByPosts() {
        saveComments();

        callAndAssertFindByPosts();
    }

    private void callAndAssertFindByPosts() {
        Page<CommentsReadDto> comments = commentsSearchService.findByPosts(post, users[0], 1);
        assertBooleanValues(comments.getContent());
    }

    private void assertBooleanValues(List<CommentsReadDto> comments) {
        List<Boolean> expectedDoesUserWriteComment = getExpectedDoesUserWriteComment();
        List<Boolean> expectedDoesUserLikeComment = getExpectedDoesUserLikeComment();

        for (int i = 0; i < comments.size(); i++) {
            CommentsReadDto comment = comments.get(i);
            assertThat(comment.isDoesUserWriteComment()).isEqualTo(expectedDoesUserWriteComment.get(i));
            assertThat(comment.isDoesUserLikeComment()).isEqualTo(expectedDoesUserLikeComment.get(i));
        }
    }

    private List<Boolean> getExpectedDoesUserWriteComment() {
        List<Boolean> expectedDoesUserWriteComment = new ArrayList<>();
        for (long i = commentsRepository.count(); i >= 1; i--) {
            Comments comment = commentsRepository.findById(i).get();
            Long authorId = comment.getUser().getId();
            Long userId = users[0].getId();
            expectedDoesUserWriteComment.add(authorId.equals(userId));
        }
        return expectedDoesUserWriteComment;
    }

    private List<Boolean> getExpectedDoesUserLikeComment() {
        List<Boolean> expectedDoesUserLikeComment = new ArrayList<>();
        for (long i = 0; i < commentsRepository.count(); i++) {
            expectedDoesUserLikeComment.add(false); // 댓글 좋아요 안 함
        }
        return expectedDoesUserLikeComment;
    }
}
