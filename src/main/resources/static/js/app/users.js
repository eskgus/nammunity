import { indexMain } from './index.js';

var usersMain = {
    init: function() {
        var _this = this;

        // 회원가입
        $('#btn-sign-up').on('click', function() {
            _this.signUp();
        });

        // username 검사
        $('#username').blur(function() {
            _this.checkUsername();
        });

        // nickname 검사
        $('#nickname').blur(function() {
            _this.checkNickname();
        });

        // password 검사
        $('#password').blur(function() {
            _this.checkPassword();
        });

        // confirmPassword 검사
        var confirmPassword = document.getElementById('confirmPassword');
        if (confirmPassword) {
            confirmPassword.addEventListener('focusin', function() {
                confirmPassword.addEventListener('input', function() {
                    _this.checkConfirmPassword('input');
                });
            });

            confirmPassword.addEventListener('focusout', function() {
                _this.checkConfirmPassword('focusout');
            });
        }

        // oldPassword 검사
        $('#oldPassword').blur(function() {
            _this.checkOldPassword();
        });

        // email 검사
        $('#email').blur(function() {
            _this.checkEmail();
        });

        // email 인증
        $('#btn-confirm-email').on('click', function() {
            _this.confirmEmail();
        });

        // 인증 이메일 재발송
        $('#btn-resend').on('click', function() {
            _this.resendEmail();
        });

        // username 찾기
        $('#btn-find-username').on('click', function(event) {
            event.preventDefault();
            _this.findUsername();
        });

        // password 찾기
        $('#btn-find-password').on('click', function(event) {
            event.preventDefault();
            _this.findPassword();
        });

        // password 변경
        $('#btn-update-password').on('click', function() {
            _this.updatePassword();
        });

        // nickname 변경
        $('#btn-update-nickname').on('click', function() {
            _this.updateNickname();
        });

        // email 변경
        $('#btn-update-email').on('click', function() {
            _this.updateEmail();
        });

        // 회원 탈퇴
        $('#btn-delete-account').on('click', function() {
            _this.deleteAccount();
        });

        // sns 연동 해제
        $('#btn-unlink-google').on('click', function() {
            _this.unlinkSocial('google');
        });

        $('#btn-unlink-naver').on('click', function() {
            _this.unlinkSocial('naver');
        });

        $('#btn-unlink-kakao').on('click', function() {
            _this.unlinkSocial('kakao');
        });
    },
    signUp: function() {
        var data = {
            username: $('#username').val(),
            password: $('#password').val(),
            confirmPassword: $('#confirmPassword').val(),
            nickname: $('#nickname').val(),
            email: $('#email').val()
        };

        var button = document.getElementById('btn-sign-up');
        button.disabled = true;

        $.ajax({
            type: 'POST',
            url: '/api/users',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            window.location.href = '/users/sign-up/' + response;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, indexMain.handleValidException);
            button.disabled = false;
        });
    },
    checkUsername: function() {
        var username = $('#username').val();
        var check = document.getElementById('ch-username');

        $.ajax({
            type: 'GET',
            url: '/api/users/validation?username=' + username
        }).done(function(response) {
            check.style = 'display: none';
        }).fail(function(xhRequest) {
            usersMain.failToCheck(xhRequest, check);
        });
    },
    failToCheck: function(xhRequest, check) {
        if (xhRequest.status === 400) { // CustomValidException
            var error = xhRequest.responseJSON[0];  // 예외 하나씩만 발생
            check.textContent = error.defaultMessage;
            check.style = 'display: inline-block';
            indexMain.redBox(error.field, error.rejectedValue);
        } else {
            indexMain.notBadRequestError(xhRequest);
        }
    },
    checkNickname: function() {
        var nickname = $('#nickname').val();
        var check = document.getElementById('ch-nickname');

        $.ajax({
            type: 'GET',
            url: '/api/users/validation?nickname=' + nickname
        }).done(function(response) {
            check.style = 'display: none';
        }).fail(function(xhRequest) {
            usersMain.failToCheck(xhRequest, check);
        });
    },
    checkPassword: function() {
        var password = $('#password').val();
        var check = document.getElementById('ch-password');
        var rb = indexMain.redBox;

        if (password == '') {
            check.textContent = '비밀번호를 입력하세요.';
            check.style = 'display: inline-block';
            rb('password', password);
        } else {
            check.style = 'display: none';
        }
    },
    checkConfirmPassword: function(event) {
        var password = $('#password').val();
        var confirmPassword = $('#confirmPassword').val();
        var box = document.getElementById('confirmPassword');
        var check = document.getElementById('ch-confirmPassword');

        var red = 'border: 2px solid #ea3636; background-color: #ffc0cb';
        var green = 'border: 1px solid #205943; background-color: #C1DAC0';
        var inlineBlock = 'display: inline-block';

        if (event == 'input') {
            if (password == confirmPassword) {
                box.style = green;
                check.style = 'display: none';
            } else {
                box.style = red;
                check.textContent = '비밀번호가 일치하지 않습니다.';
                check.style = inlineBlock;
            }
        } else if (event == 'focusout') {
            if (confirmPassword == '') {
                box.style = red;
                check.textContent = '비밀번호를 확인하세요.';
                check.style = inlineBlock;
            } else if (password != confirmPassword) {
                box.style = red;
                check.textContent = '비밀번호가 일치하지 않습니다.';
                check.style = inlineBlock;
            }
        }
    },
    checkOldPassword: function() {
        var oldPassword = $('#oldPassword').val();
        var check = document.getElementById('ch-oldPassword');
        var rb = indexMain.redBox;

        if (oldPassword == '') {
            check.textContent = '비밀번호를 입력하세요.';
            check.style = 'display: inline-block; color: red';
            rb('oldPassword', oldPassword);
        } else {
          check.style = 'display: none';
        }
    },
    checkEmail: function() {
        var email = $('#email').val();
        var check = document.getElementById('ch-email');

        $.ajax({
            type: 'GET',
            url: '/api/users/validation?email=' + email
        }).done(function(response) {
            check.style = 'display: none';
        }).fail(function(xhRequest) {
            usersMain.failToCheck(xhRequest, check);
        });
    },
    confirmEmail: function() {
        var id = $('#id').val();

        $.ajax({
            type: 'GET',
            url: '/api/users/' + id + '/confirm'
        }).done(function(response) {
            if (response.includes('/update/user-info')) {
                alert('수정됐습니다.');
            }
            window.location.href = response;
        }).fail(function(xhRequest) {
            if (xhRequest.status === 400) { // IllegalArgumentException
                alert(xhRequest.responseText);
            } else {
                indexMain.notBadRequestError(xhRequest);
            }
        });
    },
    resendEmail: function() {
        var id = $('#id').val();

        var button = document.getElementById('btn-resend');
        button.disabled = true;

        $.ajax({
            type: 'POST',
            url: '/api/users/confirm',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(id)
        }).done(function(response) {
            alert('발송됐습니다.');
            button.disabled = false;
        }).fail(function(xhRequest) {
            if (xhRequest.status === 400) { // IllegalArgumentException
                alert(xhRequest.responseText);
                button.disabled = false;
            } else {
                indexMain.notBadRequestError(xhRequest);
            }
        });
    },
    findUsername: function() {
        var email = $('#f-email').val();
        var result = document.getElementById('f-username');

        $.ajax({
            type: 'GET',
            url: '/api/users/sign-in/username?email=' + email
        }).done(function(response) {
            result.textContent = response;
            result.style = 'display: block';
        }).fail(function(xhRequest) {
            if (xhRequest.status === 400) { // IllegalArgumentException, ConstraintViolationException
                result.textContent = xhRequest.responseText;
                result.style = 'display: block; color: #ea3636';
            } else {
                indexMain.notBadRequestError(xhRequest);
            }
        });
    },
    findPassword: function() {
        var username = $('#f-username').val();
        var result = document.getElementById('f-password');

        var button = document.getElementById('btn-find-password');
        button.disabled = true;

        $.ajax({
            type: 'PUT',
            url: '/api/users/sign-in/password?username=' + username
        }).done(function(response) {
            result.textContent = '가입된 이메일로 임시 비밀번호를 발송했습니다.';
            result.style = 'display: block';
            button.textContent = '재발송';
            button.disabled = false;
        }).fail(function(xhRequest) {
            if (xhRequest.status === 400) { // IllegalArgumentException, ConstraintViolationException
                result.textContent = xhRequest.responseText;
                result.style = 'display: block; color: #ea3636';
                button.textContent = '찾기';
                button.disabled = false;
            } else {
                indexMain.notBadRequestError(xhRequest);
            }
        });
    },
    updatePassword: function() {
        var data = {
            oldPassword: $('#oldPassword').val(),
            password: $('#password').val(),
            confirmPassword: $('#confirmPassword').val()
        };

        $.ajax({
            type: 'PUT',
            url: '/api/users/password',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('비밀번호가 변경됐습니다.');
            window.location.href = '/users/my-page';
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, indexMain.handleValidException);
        });
    },
    updateNickname: function() {
        var data = {
            nickname: $('#u-nickname').val()
        };

        $.ajax({
            type: 'PUT',
            url: '/api/users/nickname',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('닉네임이 변경됐습니다.');
            window.location.reload();
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, (errors) => {
                indexMain.handleValidException(errors, false, 'u-');
            });
        });
    },
    updateEmail: function() {
        var data = {
            email: $('#u-email').val()
        };

        var button1 = document.getElementById('btn-update-email');
        var button2 = document.getElementById('btn-confirm-email');
        button1.disabled = true;

        $.ajax({
            type: 'PUT',
            url: '/api/users/email',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('발송됐습니다.');
            button1.textContent = '재발송';
            button2.style = 'display:inline-block';
            button1.disabled = false;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, (errors) => {
                indexMain.handleValidException(errors, false, 'u-');
            });
            button1.textContent = '인증';
            button1.disabled = false;
        });
    },
    deleteAccount: function() {
        $.ajax({
            type: 'DELETE',
            url: '/api/users'
        }).done(function(response) {
            alert('탈퇴됐습니다.');
            window.location.href = '/users/sign-out';
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
            window.location.href = '/users/sign-out';
        });
    },
    unlinkSocial: function(social) {
        $.ajax({
            type: 'POST',
            url: '/api/users/unlink/' + social
        }).done(function(response) {
            alert('연동 해제됐습니다.')
            window.location.reload();
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    }
};

usersMain.init();