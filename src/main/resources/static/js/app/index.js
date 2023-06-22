var main = {
    init : function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        });

        $('#btn-update').on('click', function() {
            _this.update();
        });

        $('#btn-delete').on('click', function() {
            _this.delete();
        });

        $('#btn-sign-up').on('click', function() {
            _this.signUp();
        });

        $('#username').blur(function() {
            _this.checkUsername();
        });

        $('#nickname').blur(function() {
            _this.checkNickname();
        });

        $('#password').blur(function() {
            _this.checkPassword();
        });

        $('#email').blur(function() {
            _this.checkEmail();
        });

        $('#btn-close-window').on('click', function() {
            window.close();
        });

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

        $('#oldPassword').blur(function() {
            _this.checkOldPassword();
        });

        $('#btn-back').on('click', function() {
            window.history.back();
        });

        $('#btn-confirm-email').on('click', function() {
            _this.confirmEmail();
        });

        $('#btn-resend').on('click', function() {
            _this.resendEmail();
        });

        $('#btn-find-username').on('click', function() {
            _this.findUsername();
        });

        $('#btn-find-password').on('click', function() {
            _this.findPassword();
        });

        $('#btn-update-password').on('click', function() {
            _this.updatePassword();
        });

        $('#btn-update-nickname').on('click', function() {
            _this.updateNickname();
        });

        $('#btn-update-email').on('click', function() {
            _this.updateEmail();
        });

        $('#btn-delete-account').on('click', function() {
            _this.deleteAccount();
        });

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
    save : function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var rb = this.redBoxNBC;
        var fail = this.fail;

        $.ajax({
            type: 'POST',
            url: '/api/posts',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                alert('글이 등록되었습니다.');
                window.location.href = '/';
            } else {
                fail(response, data, rb);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    update : function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();
        var rb = this.redBoxNBC;
        var fail = this.fail;

        $.ajax({
            type: 'PUT',
            url: '/api/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                alert('글이 수정되었습니다.');
                window.location.href = '/posts/read/' + id;
            } else if (Object.keys(response) == 'error') {
                alert(response[Object.keys(response)]);
                window.location.href = '/';
            } else {
                fail(response, data, rb);
            }
        }).fail(function(response) {
            if (response.status == 403) {
                alert('권한이 없습니다.');
                window.history.back();
            } else {
                alert(JSON.stringify(response));
                window.location.href = '/';
            }
        });
    },
    delete : function() {
        var id = $('#id').val();

        $.ajax({
            type: 'DELETE',
            url: '/api/posts/' + id,
            contentType: 'application/json; charset=utf-8'
        }).done(function() {
            alert('글이 삭제되었습니다.');
            window.location.href = '/';
        }).fail(function(response) {
            if (response.status == 403) {
                alert('권한이 없습니다.');
                window.history.back();
            } else {
                alert(JSON.stringify(response));
            }
        });
    },
    signUp : function() {
        var data = {
            username: $('#username').val(),
            password: $('#password').val(),
            confirmPassword: $('#confirmPassword').val(),
            nickname: $('#nickname').val(),
            email: $('#email').val()
        };

        var rb = this.redBox;
        var fail = this.fail;

        var button = document.getElementById('btn-sign-up');
        button.disabled = true;

        $.ajax({
            type: 'POST',
            url: '/api/users',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                var id = response[Object.keys(response)];
                window.location.href = '/users/sign-up/' + id;
            } else {
                fail(response, data, rb);
                button.disabled = false;
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
            button.disabled = false;
        });
    },
    checkUsername : function() {
        var username = $('#username').val();
        var check = document.getElementById('ch-username');
        var rb = this.redBox;

        $.ajax({
            type: 'GET',
            url: '/api/users?username=' + username
        }).done(function(response) {
            if (response == 'OK') {
                check.style = 'display: none';
            } else {
                check.textContent = response;
                check.style = 'display: block; color: red';
                rb('username', username);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response))
        });
    },
    checkNickname : function() {
        var nickname = $('#nickname').val();
        var check = document.getElementById('ch-nickname');
        var rb = this.redBox;

        $.ajax({
            type: 'GET',
            url: '/api/users?nickname=' + nickname
        }).done(function(response) {
            if (response == 'OK') {
                check.style = 'display: none';
            } else {
                check.textContent = response;
                check.style = 'display: block; color: red';
                rb('nickname', nickname);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response))
        });
    },
    checkPassword : function() {
        var password = $('#password').val();
        var check = document.getElementById('ch-password');
        var rb = this.redBox;

        if (password == '') {
            check.textContent = '비밀번호를 입력하세요.';
            check.style = 'display: block; color: red';
            rb('password', password);
        } else {
            check.style = 'display: none';
        }
    },
    checkConfirmPassword : function(event) {
        var password = $('#password').val();
        var confirmPassword = $('#confirmPassword').val();
        var box = document.getElementById('confirmPassword');
        var check = document.getElementById('ch-confirmPassword');

        if (event == 'input') {
            if (password == confirmPassword) {
                box.style = 'border: 1px solid #AAC2A9; background-color: #C1DAC0';
                check.style = 'display: none';
            } else {
                box.style = 'border: 1px solid red; background-color: pink';
                check.textContent = '비밀번호가 일치하지 않습니다.';
                check.style = 'display: block; color: red';
            }
        } else if (event == 'focusout') {
            if (confirmPassword == '') {
                box.style = 'border: 1px solid red; background-color: pink';
                check.textContent = '비밀번호를 확인하세요.';
                check.style = 'display: block; color: red';
            } else if (password != confirmPassword) {
                box.style = 'border: 1px solid red; background-color: pink';
                check.textContent = '비밀번호가 일치하지 않습니다.';
                check.style = 'display: block; color: red';
            }
        }
    },
    checkOldPassword : function() {
        var oldPassword = $('#oldPassword').val();
        var check = document.getElementById('ch-oldPassword');
        var rb = this.redBox;

        if (oldPassword == '') {
            check.textContent = '비밀번호를 입력하세요.';
            check.style = 'display: block; color: red';
            rb('oldPassword', oldPassword);
        } else {
          check.style = 'display: none';
        }
    },
    checkEmail : function() {
        var email = $('#email').val();
        var check = document.getElementById('ch-email');
        var rb = this.redBox;

        $.ajax({
            type: 'GET',
            url: '/api/users?email=' + email
        }).done(function(response) {
            if (response == 'OK') {
                check.style = 'display: none';
            } else {
                check.textContent = response;
                check.style = 'display: block; color: red';
                rb('email', email);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response))
        });
    },
    confirmEmail : function() {
        var id = $('#id').val();

        $.ajax({
            type: 'GET',
            url: '/api/users/confirm/' + id
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                var url = response[Object.keys(response)];
                if (url.includes('update/user-info')) {
                    alert('수정 완료');
                }
                window.location.href = url;
            } else {
                alert(response[Object.keys(response)]);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    resendEmail: function() {
        var data = {
            id: $('#id').val()
        };

        var button = document.getElementById('btn-resend');
        button.disabled = true;

        $.ajax({
            type: 'POST',
            url: '/api/users/confirm',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert(response[Object.keys(response)]);
            button.disabled = false;
        }).fail(function(response) {
            alert(JSON.stringify(response));
            button.disabled = false;
        });
    },
    findUsername : function() {
        var email = $('#f-email').val();
        var result = document.getElementById('f-username');

        $.ajax({
            type: 'GET',
            url: '/api/users/sign-in?email=' + email
        }).done(function(response) {
            result.textContent = response[Object.keys(response)];
            result.style = 'display: block';
            if (Object.keys(response) != 'OK') {
                result.style = 'color: red';
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    findPassword : function() {
        var username = $('#f-username').val();
        var result = document.getElementById('f-password');

        var button = document.getElementById('btn-find-password');
        button.disabled = true;

        $.ajax({
            type: 'PUT',
            url: '/api/users/sign-in?username=' + username
        }).done(function(response) {
            result.textContent = response[Object.keys(response)];
            result.style = 'display:block';
            if (Object.keys(response) != 'OK') {
                result.style = 'color: red';
                button.textContent = '찾기';
            } else {
                button.textContent = '재발송';
            }
            button.disabled = false;
        }).fail(function(response) {
            alert(JSON.stringify(response));
            button.disabled = false;
        });
    },
    updatePassword : function() {
        var data = {
            oldPassword: $('#oldPassword').val(),
            password: $('#password').val(),
            confirmPassword: $('#confirmPassword').val()
        };

        var rb = this.redBox;
        var fail = this.fail;

        $.ajax({
            type: 'PUT',
            url: '/api/users/update/password',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                alert(response[Object.keys(response)]);
                window.location.href = '/users/my-page';
            } else if (Object.keys(response) == 'username') {
                alert(response[Object.keys(response)]);
            } else {
                fail(response, data, rb);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    updateNickname : function() {
        var data = {
            nickname: $('#u-nickname').val()
        };

        $.ajax({
            type: 'PUT',
            url: '/api/users/update/nickname',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert(response[Object.keys(response)]);
            if (Object.keys(response) == 'OK') {
                window.location.reload();
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    updateEmail : function() {
        var data = {
            email: $('#u-email').val()
        };

        var button1 = document.getElementById('btn-update-email');
        var button2 = document.getElementById('btn-confirm-email');
        button1.disabled = true;

        $.ajax({
            type: 'PUT',
            url: '/api/users/update/email',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert(response[Object.keys(response)]);
            if (Object.keys(response) == 'OK') {
                button1.textContent = '재발송';
                button2.style = 'display: inline-block';
            } else {
                button1.textContent = '인증';
            }
            button1.disabled = false;
        }).fail(function(response) {
            alert(JSON.stringify(response));
            button1.disabled = false;
        });
    },
    deleteAccount : function() {
        $.ajax({
            type: 'DELETE',
            url: '/api/users/delete'
        }).done(function(response) {
            alert(response[Object.keys(response)]);
            if (Object.keys(response) == 'OK') {
                window.location.href = '/users/sign-out';
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    unlinkSocial : function(social) {
        $.ajax({
            type: 'POST',
            url: '/api/users/unlink/' + social
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                alert(response[Object.keys(response)]);
                window.location.reload();
            } else {
                alert(response[Object.keys(response)]);
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    redBox : function(field, pre) {
        var box = document.getElementById(field);

        box.style = 'border: 1px solid red; background-color: pink';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #ced4da';
            } else {
                box.style = 'border: 1px solid red; background-color: pink';
            }
        });
    },
    redBoxNBC : function(field, pre) {
        var box = document.getElementById(field);

        box.style = 'border: 1px solid red';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #ced4da';
            } else {
                box.style = 'border: 1px solid red';
            }
        });
    },
    fail: function (response, data, rb) {
        var size = Object.keys(response).length;
        let firstData = 4;

        for (let i = 0; i < size; i++) {
            let error = Object.keys(response)[i];
            let index = Object.keys(data).indexOf(error);
            firstData = firstData > index ? index : firstData;
            rb(error, data[Object.keys(data)[index]]);
        }

        var firstError = Object.keys(data)[firstData];
        alert(response[firstError]);
        document.getElementById(Object.keys(data)[firstData]).focus();
    }
};

main.init();