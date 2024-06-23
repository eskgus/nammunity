package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessages {
    EMPTY_USERNAME("ID를 입력하세요."),
    EMPTY_PASSWORD("비밀번호를 입력하세요."),
    EMPTY_OLD_PASSWORD("현재 비밀번호를 입력하세요."),
    EMPTY_NEW_PASSWORD("새 비밀번호를 입력하세요."),
    EMPTY_CONFIRM_PASSWORD("비밀번호를 확인하세요."),
    EMPTY_NICKNAME("닉네임을 입력하세요."),
    EMPTY_EMAIL("이메일을 입력하세요."),

    EMPTY_TITLE("제목을 입력하세요."),
    EMPTY_CONTENT("내용을 입력하세요."),
    EMPTY_COMMENT("댓글을 입력하세요."),

    EMPTY_REASONS_ID("신고 사유를 선택하세요."),
    EMPTY_CONTENT_IDS("삭제할 항목을 선택하세요."),

    MISMATCH_OLD_PASSWORD("현재 비밀번호가 일치하지 않습니다."),
    MISMATCH_CONFIRM_PASSWORD("비밀번호가 일치하지 않습니다."),

    INVALID_USERNAME("ID는 영어 소문자로 시작, 숫자 1개 이상 포함, 한글/특수문자/공백 불가능, 8글자 이상 20글자 이하"),
    INVALID_PASSWORD("비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하"),
    INVALID_NEW_PASSWORD("현재 비밀번호와 새 비밀번호가 같으면 안 됩니다."),
    INVALID_NICKNAME("닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하"),
    INVALID_EMAIL("이메일 형식이 맞지 않습니다."),

    INVALID_TITLE("글 제목은 100글자 이하여야 합니다."),
    INVALID_CONTENT("글 내용은 3000글자 이하여야 합니다."),
    INVALID_COMMENT("댓글은 1500글자 이하여야 합니다."),

    INVALID_OTHER_REASONS("기타 사유는 500글자 이하로 작성해 주세요."),

    NON_EXISTENT_USER("존재하지 않는 ID입니다."),
    NON_EXISTENT_POST("해당 게시글이 없습니다."),
    NON_EXISTENT_COMMENT("해당 댓글이 없습니다."),

    EXISTENT_USERNAME("이미 사용 중인 ID입니다."),
    EXISTENT_NICKNAME("이미 사용 중인 닉네임입니다."),
    EXISTENT_EMAIL("이미 사용 중인 이메일입니다."),

    RESEND_NOT_ALLOWED("더 이상 재발송할 수 없어요. 다시 가입해 주세요."),

    UNAUTHORIZED("로그인하세요."),
    FORBIDDEN("권한이 없습니다."),

    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다. 관리자에게 문의하세요."),

    ILLEGAL_ARGUMENT_EXCEPTION_TEST("IllegalArgumentException"),
    CUSTOM_VALID_EXCEPTION_TEST("CustomValidException");

    private final String message;
}
