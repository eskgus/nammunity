package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.util.FinderUtil;
import com.eskgus.nammunity.util.SearchUtil;
import com.eskgus.nammunity.util.TestDB;
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

import java.time.LocalDateTime;
import java.util.*;

import static com.eskgus.nammunity.util.FinderUtil.callAndAssertFindContentsByUser;
import static com.eskgus.nammunity.util.SearchUtil.callAndAssertSearchByField;
import static com.eskgus.nammunity.util.SearchUtil.getExpectedIdList;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void save() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. title, content, user로 Posts 생성
        String title = "title";
        String content = "content";
        Posts post = Posts.builder().title(title).content(content).user(user1).build();

        // 3. post로 save() 호출
        postsRepository.save(post);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 4. post id로 savedPost 찾고
        Optional<Posts> result = postsRepository.findById(post.getId());
        Assertions.assertThat(result).isPresent();

        // 5. 찾은 savedPost의 title, content, user 확인
        Posts savedPost = result.get();
        Assertions.assertThat(savedPost.getTitle()).isEqualTo(title);
        Assertions.assertThat(savedPost.getContent()).isEqualTo(content);
        Assertions.assertThat(savedPost.getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    public void addBaseTimeEntity() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. 저장된 게시글의 createdDate, modifiedDate가 현재 시각 이전인지 확인
        LocalDateTime now = LocalDateTime.now();

        Optional<Posts> result = postsRepository.findById(postId);
        Assertions.assertThat(result).isPresent();
        Posts savedPost = result.get();

        Assertions.assertThat(savedPost.getCreatedDate()).isBefore(now);
        Assertions.assertThat(savedPost.getModifiedDate()).isBefore(now);
    }

    @Test
    public void countByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. 게시글 작성 x 후 호출
        callAndAssertCountByUser(user1);

        // 3. 게시글 1개 작성 후 호출
        // 3-1. user1이 게시글 작성
        testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        callAndAssertCountByUser(user1);
    }

    @Test
    public void searchByTitle() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 2
        String str1 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str2 = "붕어빵 3마리 1000원";
        testDB.savePosts(user1, str1, str2);
        Assertions.assertThat(postsRepository.count()).isEqualTo(4);

        // 3. 예상 결과(List<Posts>) 생성
        // 3-1. 전체 게시글, 검색어, 검색 제외 단어 준비
        List<Posts> posts = postsRepository.findAll();
        String[] includeKeywords = { "흥", "100", "Let" };
        String[] excludeKeywords = { "봉", "마리" };

        // 3-2. 검색 제외 단어 없이 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto1 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getTitle).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).build();
        List<Long> expectedIdList1 = getExpectedIdList(searchDto1);

        // 3-3. 검색 제외 단어 포함해서 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto2 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getTitle).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).excludeKeywords(excludeKeywords).build();
        List<Long> expectedIdList2 = getExpectedIdList(searchDto2);

        // 4. searchByTitle() 호출
        // 4-1. 검색 제외 단어 x
        callAndAssertSearchByField("흥 100 Let", postsRepository::searchByTitle,
                searchDto1.getIdExtractor(), expectedIdList1);

        // 4-2. 검색 제외 단어 o
        callAndAssertSearchByField("흥 100 Let -봉,마리", postsRepository::searchByTitle,
                searchDto2.getIdExtractor(), expectedIdList2);
    }

    @Test
    public void searchByContent() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 2
        String str1 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str2 = "붕어빵 3마리 1000원";
        testDB.savePosts(user1, str1, str2);
        Assertions.assertThat(postsRepository.count()).isEqualTo(4);

        // 3. 예상 결과(List<Posts>) 생성
        // 3-1. 전체 게시글, 검색어, 검색 제외 단어 준비
        List<Posts> posts = postsRepository.findAll();
        String[] includeKeywords = { "흥", "100", "Let" };
        String[] excludeKeywords = { "봉", "마리" };

        // 3-2. 검색 제외 단어 없이 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto1 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getContent).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).build();
        List<Long> expectedIdList1 = getExpectedIdList(searchDto1);

        // 3-3. 검색 제외 단어 포함해서 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto2 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getContent).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).excludeKeywords(excludeKeywords).build();
        List<Long> expectedIdList2 = getExpectedIdList(searchDto2);

        // 4. searchByContent() 호출
        // 4-1. 검색 제외 단어 x
        callAndAssertSearchByField("흥 100 Let", postsRepository::searchByContent,
                searchDto1.getIdExtractor(), expectedIdList1);

        // 4-2. 검색 제외 단어 o
        callAndAssertSearchByField("흥 100 Let -봉,마리", postsRepository::searchByContent,
                searchDto2.getIdExtractor(), expectedIdList2);
    }

    @Test
    public void searchByTitleAndContent() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 9
        String str1 = "default";
        String str2 = "bts, 봉준호, 손흥민, 이나현 let's go";
        String str3 = "붕어빵 3마리 1000원";
        testDB.savePosts(user1, str1, str2, str3);
        Assertions.assertThat(postsRepository.count()).isEqualTo(9);

        // 3. 예상 결과(List<Posts>) 생성
        // 3-1. 전체 게시글, 검색어, 검색 제외 단어 준비
        List<Posts> posts = postsRepository.findAll();
        String[] includeKeywords = { "흥", "100", "Let" };
        String[] excludeKeywords = { "봉" };

        // 3-2. 검색 제외 단어 없이 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto1 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getTitle).contentExtractor(Posts::getContent).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).build();
        List<Long> expectedIdList1 = getExpectedIdList(searchDto1);

        // 3-3. 검색 제외 단어 포함해서 호출할 때 예상 결과
        SearchUtil.SearchDto<Posts> searchDto2 = SearchUtil.SearchDto.<Posts>builder()
                .contents(posts).fieldExtractor(Posts::getTitle).contentExtractor(Posts::getContent).idExtractor(Posts::getId)
                .includeKeywords(includeKeywords).excludeKeywords(excludeKeywords).build();
        List<Long> expectedIdList2 = getExpectedIdList(searchDto2);

        // 4. searchByTitleAndContent() 호출
        // 4-1. 검색 제외 단어 x
        callAndAssertSearchByField("흥 100 Let", postsRepository::searchByTitleAndContent,
                searchDto1.getIdExtractor(), expectedIdList1);

        // 4-2. 검색 제외 단어 o
        callAndAssertSearchByField("흥 100 Let -봉", postsRepository::searchByTitleAndContent,
                searchDto2.getIdExtractor(), expectedIdList2);
    }

    @Test
    public void findAllDesc() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 4
        String str1 = "title1";
        String str2 = "title2";
        testDB.savePosts(user1, str1, str2);
        Assertions.assertThat(postsRepository.count()).isEqualTo(4);

        long numOfPosts = postsRepository.count();

        // 3. findAllDesc() 호출
        List<Posts> result = postsRepository.findAllDesc();
        Assertions.assertThat(result.size()).isEqualTo(numOfPosts);

        // 4. 결과로 얻은 List<Posts>의 id가 내림차순인지 확인
        List<Long> expectedIdList = new ArrayList<>();
        for (long i = numOfPosts; i > 0; i--) {
            expectedIdList.add(i);
        }

        Assertions.assertThat(result).extracting(Posts::getId).isEqualTo(expectedIdList);
    }

    @Test
    public void findByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1이 게시글 작성 * 2 + user2가 게시글 작성 * 2
        // 3. List<Long> expectedIdList에 user1이 작성한 게시글 id 내림차순 저장
        List<User> users = Arrays.asList(user1, user2);
        List<Long> expectedIdList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (User user : users) {
                Long postId = testDB.savePosts(user);
                if (user.equals(user1)) {
                    expectedIdList.add(postId);
                }
            }
        }
        Assertions.assertThat(postsRepository.count()).isEqualTo(4);

        // 4. user1로 findByUser() 호출 + 검증
        FinderUtil.FindDto<Posts> findDto = FinderUtil.FindDto.<Posts>builder()
                .user(user1).finder(postsRepository::findByUser).expectedIdList(expectedIdList).build();
        callAndAssertFindContentsByUser(findDto);
    }

    private void callAndAssertCountByUser(User user) {
        // 1. expectedCount에 현재 저장된 게시글 수 저장
        long expectedCount = postsRepository.count();

        // 2. user로 countByUser() 호출하고 리턴 값 actualCount에 저장
        long actualCount = postsRepository.countByUser(user);

        // 3. actualCount가 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }
}
