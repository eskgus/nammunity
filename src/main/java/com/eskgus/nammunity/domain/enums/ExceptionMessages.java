package com.eskgus.nammunity.domain.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionMessages {
    // 로그인
    UNAUTHORIZED("로그인하세요."),
    FORBIDDEN("권한이 없습니다."),
    BANNED("활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요."),
    LOCKED("로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요."),
    BAD_CREDENTIALS("ID가 존재하지 않거나 비밀번호가 일치하지 않습니다."),
    DISABLED("이메일 인증이 되지 않은 계정입니다. 이메일 인증을 완료하세요."),
    ACCOUNT_EXPIRED,
    CREDENTIALS_EXPIRED,

    // user
    USERNAME_NOT_FOUND,
    USER_NOT_FOUND,
    EMAIL_NOT_FOUND,

    TOKEN_NOT_FOUND,

    USERNAME_EXISTS,
    NICKNAME_EXISTS,
    EMAIL_EXISTS,

    SOCIAL_ACCOUNT_EXISTS,

    EMPTY_USERNAME,
    EMPTY_PASSWORD,
    EMPTY_CONFIRM_PASSWORD,
    EMPTY_NICKNAME,
    EMPTY_EMAIL,

    EMPTY_OLD_PASSWORD,
    EMPTY_NEW_PASSWORD,

    INVALID_USERNAME,
    INVALID_PASSWORD,
    INVALID_NICKNAME,
    INVALID_EMAIL,

    INVALID_NEW_PASSWORD,
    INVALID_NEW_NICKNAME,
    INVALID_NEW_EMAIL,

    OLD_PASSWORD_MISMATCH,
    CONFIRM_PASSWORD_MISMATCH,

    EMAIL_CONFIRMED,
    EMAIL_NOT_CONFIRMED,
    TOKEN_EXPIRED,
    RESEND_NOT_ALLOWED("더 이상 재발송할 수 없습니다. 다시 가입해 주세요."),

    // post
    POST_NOT_FOUND,

    EMPTY_TITLE,
    EMPTY_CONTENT,

    INVALID_TITLE,
    INVALID_CONTENT,

    // comment
    COMMENT_NOT_FOUND,

    EMPTY_COMMENT,

    INVALID_COMMENT,

    // like
    LIKE_NOT_FOUND,

    EMPTY_CONTENTS,

    // report
    REASON_NOT_FOUND,
    TYPE_NOT_FOUND,
    REPORT_SUMMARY_NOT_FOUND,

    EMPTY_TYPE,
    EMPTY_REASON,
    EMPTY_OTHER_REASON,

    INVALID_OTHER_REASON,

    // 500
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다. 관리자에게 문의하세요."),

    // test
    ILLEGAL_ARGUMENT_EXCEPTION_TEST("IllegalArgumentException"),
    CUSTOM_VALID_EXCEPTION_TEST("CustomValidException");

    private String message;

    public String getMessage() {
        if (message != null) {
            return message;
        } else {
            return createMessage();
        }
    }

    private String createMessage() {
        String name = this.toString().toLowerCase();
        String fix = getFix(name);
        String field = getField(name);

        return String.format(fix, field);
    }

    private String getFix(String name) {
        if (name.startsWith("empty")) {
            return getEmptyFix(name);
        } else if (name.startsWith("invalid")) {
            return getInvalidFix(name);
        } else if (name.endsWith("confirmed")) {
            return getConfirmedFix(name);
        } else if (name.endsWith("expired")) {
            return "만료된 %s입니다.";
        } else if (name.endsWith("not_found")) {
            return "%s이(가) 존재하지 않습니다.";
        } else if (name.endsWith("exists")) {
            return "이미 사용 중인 %s입니다.";
        } else if (name.endsWith("mismatch")) {
            return "%s가 일치하지 않습니다.";
        }
        return "";
    }

    private String getField(String name) {
        if (name.contains("account")) {
            return "계정";
        } else if (name.startsWith("credentials") || name.contains("password")) {
            return getPasswordField(name);
        } else if (name.startsWith("token")) {
            return "인증 링크";
        } else if (name.contains("username")) {
            return "ID";
        } else if (name.startsWith("user")) {
            return "회원";
        } else if (name.contains("email")) {
            return "이메일";
        } else if (name.startsWith("post")) {
            return "게시글";
        } else if (name.contains("comment")) {
            return getCommentField(name);
        } else if (name.startsWith("like")) {
            return "좋아요";
        } else if (name.endsWith("other_reason")) {
            return getOtherReasonField(name);
        } else if (name.contains("reason")) {
            return "신고 사유";
        } else if (name.contains("type")) {
            return "신고 분류";
        } else if (name.startsWith("report_summary")) {
            return "신고 요약 내역";
        } else if (name.contains("nickname")) {
            return "닉네임";
        } else if (name.endsWith("title")) {
            return getTitleField(name);
        } else if (name.endsWith("content")) {
            return getContentField(name);
        } else if (name.endsWith("contents")) {
            return "삭제할 항목";
        }
        return "";
    }

    private String getEmptyFix(String name) {
        if (name.endsWith("contents") || name.endsWith("type") || name.endsWith("reason") && !name.contains("other")) {
            return "%s을(를) 선택하세요.";
        } else {
            return  "%s을(를) 입력하세요.";
        }
    }

    private String getInvalidFix(String name) {
        if (name.contains("new")) {
            return "현재 %s와(과) 같습니다.";
        } else if (name.endsWith("title") || name.endsWith("content") || name.endsWith("comment") || name.endsWith("other_reason")) {
            return "%s글자 이하로 작성하세요.";
        } else {
            return "%s 형식을 확인하세요.";
        }
    }

    private String getConfirmedFix(String name) {
        if (!name.contains("not")) {
            return "이미 인증된 %s입니다.";
        } else {
            return "인증되지 않은 %s입니다.";
        }
    }

    private String getPasswordField(String name) {
        String field = "비밀번호";
        if (name.startsWith("empty_confirm")) {
            return field + " 확인";
        } else if (name.contains("old")) {
            return "현재 " + field;
        } else if (name.startsWith("empty_new")) {
            return "새 " + field;
        } else {
            return field;
        }
    }

    private String getCommentField(String name) {
        String field = "댓글";
        if (name.startsWith("invalid")) {
            return field + "은 1500";
        } else {
            return field;
        }
    }

    private String getOtherReasonField(String name) {
        String field = "기타 사유";
        if (name.startsWith("invalid")) {
            return field + "는 500";
        } else {
            return field;
        }
    }

    private String getTitleField(String name) {
        String field = "제목";
        if (name.startsWith("invalid")) {
            return field + "은 100";
        } else {
            return field;
        }
    }

    private String getContentField(String name) {
        String field = "내용";
        if (name.startsWith("invalid")) {
            return field + "은 3000";
        } else {
            return field;
        }
    }
}
