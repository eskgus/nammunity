package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessages {
    // 로그인
    UNAUTHORIZED("로그인하세요."),
    FORBIDDEN("권한이 없습니다."),
    BANNED("활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요."),
    LOCKED("로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요."),
    BAD_CREDENTIALS("ID가 존재하지 않거나 비밀번호가 일치하지 않습니다."),
    DISABLED("이메일 인증이 되지 않은 계정입니다. 이메일 인증을 완료하세요."),
    ACCOUNT_EXPIRED("만료된 계정입니다."),
    CREDENTIALS_EXPIRED("만료된 비밀번호입니다."),

    // user
    USERNAME_NOT_FOUND("존재하지 않는 ID입니다."),
    USER_NOT_FOUND("존재하지 않는 회원입니다."),
    EMAIL_NOT_FOUND("가입되지 않은 이메일입니다."),

    TOKEN_NOT_FOUND("인증 링크가 존재하지 않습니다."),

    USERNAME_EXISTS("이미 사용 중인 ID입니다."),
    NICKNAME_EXISTS("이미 사용 중인 닉네임입니다."),
    EMAIL_EXISTS("이미 사용 중인 이메일입니다."),

    SOCIAL_ACCOUNT_EXISTS("연동할 계정을 사용 중인 다른 사용자가 있습니다."),

    EMPTY_USERNAME("ID를 입력하세요."),
    EMPTY_PASSWORD("비밀번호를 입력하세요."),
    EMPTY_CONFIRM_PASSWORD("비밀번호를 확인하세요."),
    EMPTY_NICKNAME("닉네임을 입력하세요."),
    EMPTY_EMAIL("이메일을 입력하세요."),

    EMPTY_OLD_PASSWORD("현재 비밀번호를 입력하세요."),
    EMPTY_NEW_PASSWORD("새 비밀번호를 입력하세요."),

    INVALID_USERNAME("ID는 영어 소문자로 시작, 숫자 1개 이상 포함, 한글/특수문자/공백 불가능, 8글자 이상 20글자 이하"),
    INVALID_PASSWORD("비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하"),
    INVALID_NICKNAME("닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하"),
    INVALID_EMAIL("이메일 형식이 맞지 않습니다."),

    INVALID_NEW_PASSWORD("현재 비밀번호와 새 비밀번호가 같으면 안 됩니다."),
    INVALID_NEW_NICKNAME("현재 닉네임과 같습니다."),
    INVALID_NEW_EMAIL("현재 이메일과 같습니다."),

    OLD_PASSWORD_MISMATCH("현재 비밀번호가 일치하지 않습니다."),
    CONFIRM_PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),

    EMAIL_CONFIRMED("이미 인증된 이메일입니다."),
    EMAIL_NOT_CONFIRMED("인증되지 않은 이메일입니다."),
    TOKEN_EXPIRED("인증 링크가 만료됐습니다."),
    RESEND_NOT_ALLOWED("더 이상 재발송할 수 없어요. 다시 가입해 주세요."),

    // post
    POST_NOT_FOUND("해당 게시글이 없습니다."),

    EMPTY_TITLE("제목을 입력하세요."),
    EMPTY_CONTENT("내용을 입력하세요."),

    INVALID_TITLE("글 제목은 100글자 이하여야 합니다."),
    INVALID_CONTENT("글 내용은 3000글자 이하여야 합니다."),

    // comment
    COMMENT_NOT_FOUND("해당 댓글이 없습니다."),

    EMPTY_COMMENT("댓글을 입력하세요."),

    INVALID_COMMENT("댓글은 1500글자 이하여야 합니다."),

    // like
    LIKE_NOT_FOUND("해당 좋아요가 없습니다."),

    EMPTY_CONTENT_IDS("삭제할 항목을 선택하세요."),

    // report
    REASON_ID_NOT_FOUND("해당 신고 사유가 없습니다."),
    TYPE_NOT_FOUND("해당 분류가 없습니다."),
    USER_REPORT_SUMMARY_NOT_FOUND("신고 요약 내역이 존재하지 않는 회원입니다."),

    EMPTY_TYPE("신고 분류가 선택되지 않았습니다."),
    EMPTY_REASON_ID("신고 사유를 선택하세요."),
    EMPTY_OTHER_REASON("기타 사유를 입력하세요."),

    INVALID_OTHER_REASON("기타 사유는 500글자 이하로 작성해 주세요."),

    // 500
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다. 관리자에게 문의하세요."),

    // test
    ILLEGAL_ARGUMENT_EXCEPTION_TEST("IllegalArgumentException"),
    CUSTOM_VALID_EXCEPTION_TEST("CustomValidException");

    private final String message;
}
