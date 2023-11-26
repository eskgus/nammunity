package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.util.SearchUtil;
import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static com.eskgus.nammunity.util.SearchUtil.callAndAssertSearchByField;
import static com.eskgus.nammunity.util.SearchUtil.getExpectedIdList;

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

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. 댓글 작성 x 후 호출
        callAndAssertCountByUser(user1);

        // 4. 댓글 1개 작성 후 호출
        // 4-1. user1이 댓글 작성
        testDB.saveComments(post.getId(), user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        callAndAssertCountByUser(user1);
    }

    @Test
    public void searchByContent() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성 * 2
        String str1 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str2 = "붕어빵 3마리 1000원";
        List<String> strings = Arrays.asList(str1, str2);
        for (String content : strings) {
            testDB.saveComments(post.getId(), content, user1);
        }
        Assertions.assertThat(commentsRepository.count()).isEqualTo(2);

        // 4. 예상 결과(List<Comments>) 생성
        // 4-1. 전체 댓글, 검색어, 검색 제외 단어 준비
        List<Comments> comments = commentsRepository.findAll();
        String[] includeKeywords = { "흥", "100", "Let" };
        String[] excludeKeywords = { "봉", "마리" };

        // 4-2. 검색 제외 단어 없이 호출할 때 예상 결과
        SearchUtil.SearchDto<Comments> searchDto1 = SearchUtil.SearchDto.<Comments>builder()
                .contents(comments).fieldExtractor(Comments::getContent).idExtractor(Comments::getId)
                .includeKeywords(includeKeywords).build();
        List<Long> expectedIdList1 = getExpectedIdList(searchDto1);

        // 4-3. 검색 제외 단어 포함해서 호출할 때 예상 결과
        SearchUtil.SearchDto<Comments> searchDto2 = SearchUtil.SearchDto.<Comments>builder()
                .contents(comments).fieldExtractor(Comments::getContent).idExtractor(Comments::getId)
                .includeKeywords(includeKeywords).excludeKeywords(excludeKeywords).build();
        List<Long> expectedIdList2 = getExpectedIdList(searchDto2);

        // 5. searchByContent() 호출
        // 5-1. 검색 제외 단어 x
        callAndAssertSearchByField("흥 100 Let", commentsRepository::searchByContent,
                searchDto1.getIdExtractor(), expectedIdList1);

        // 5-2. 검색 제외 단어 o
        callAndAssertSearchByField("흥 100 Let -봉,마리", commentsRepository::searchByContent,
                searchDto2.getIdExtractor(), expectedIdList2);
    }

    private void callAndAssertCountByUser(User user) {
        // 1. expectedCount에 현재 저장된 댓글 수 저장
        long expectedCount = commentsRepository.count();

        // 2. user로 countByUser() 호출하고 리턴 값 actualCount에 저장
        long actualCount = commentsRepository.countByUser(user);

        // 3. actualCount가 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }
}
