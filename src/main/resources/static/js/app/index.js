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

        $('#btn-confirm-email').on('click', function() {
            window.close();
        });

        var confirmPassword = document.getElementById('confirm-password');
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
    },
    save : function() {
        var data = {
            title: $('#title').val(),
            author: $('#author').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/posts',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href = '/';
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },
    update : function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();

        $.ajax({
            type: 'PUT',
            url: '/api/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = '/posts/read/' + id;
        }).fail(function(error) {
            alert(JSON.stringify(error));
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
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },
    signUp : function() {
        var data = {
            username: $('#username').val(),
            password: $('#password').val(),
            nickname: $('#nickname').val(),
            confirmPassword: $('#confirm-password').val(),
            email: $('#email').val()
        };

        var rb = this.redBox;
        var rbwf = this.redBoxWof;

        $.ajax({
            type: 'POST',
            url: '/api/users',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('회원가입이 완료됐습니다.');
            window.location.href = '/';
        }).fail(function(response) {
            var error = response.responseJSON;

            if (error.username) {
                alert(error.username);
                rb('username', data.username);
            } else if (error.password) {
                alert(error.password);
                rb('password', data.password);
                rbwf('confirm-password', data.confirmPassword);
            } else if (error.confirmPassword) {
                alert(error.confirmPassword);
                rb('confirm-password', data.confirmPassword);
            } else if (error.nickname) {
                alert(error.nickname);
                rb('nickname', data.nickname);
            } else if (error.email) {
                alert(error.email);
                rb('email', data.email);
            }
        });
    },
    checkUsername : function() {
        var username = $('#username').val();
        var check = document.getElementById('ch-username');
        var rb = this.redBoxWof;

        $.ajax({
            type: 'GET',
            url: '/api/users/exists/username/' + username
        }).done(function(response) {
            if (response == false) {
                check.style = 'display: none';
            } else {
                check.textContent = '이미 사용 중인 ID입니다.';
                check.style = 'display: block; color: red';
                rb('username', username);
            }
        }).fail(function(response) {
            if (response.status == 401) {
                check.textContent = 'ID를 입력하세요.';
                check.style = 'display: block; color: red';
                rb('username', username);
            }
        });
    },
    checkNickname : function() {
        var nickname = $('#nickname').val();
        var check = document.getElementById('ch-nickname');
        var rb = this.redBoxWof;

        $.ajax({
            type: 'GET',
            url: '/api/users/exists/nickname/' + nickname
        }).done(function(response) {
            if (response == false) {
                check.style = 'display: none';
            } else {
                check.textContent = '이미 사용 중인 닉네임입니다.';
                check.style = 'display: block; color: red';
                rb('nickname', nickname);
            }
        }).fail(function(response) {
            if (response.status == 401) {
                check.textContent = '닉네임을 입력하세요.';
                check.style = 'display: block; color: red';
                rb('nickname', nickname);
            }
        });
    },
    checkPassword : function() {
        var password = $('#password').val();
        var check = document.getElementById('ch-password');
        var rb = this.redBoxWof;

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
        var confirmPassword = $('#confirm-password').val();
        var box = document.getElementById('confirm-password');
        var check = document.getElementById('ch-confirm-password');

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
    checkEmail : function() {
        var email = $('#email').val();
        var check = document.getElementById('ch-email');
        var rb = this.redBoxWof;

        $.ajax({
            type: 'GET',
            url: '/api/users/exists/email/' + email
        }).done(function(response) {
            if (response == false) {
                check.style = 'display: none';
            } else {
                check.textContent = '이미 사용 중인 이메일입니다.';
                check.style = 'display: block; color: red';
                rb('email', email);
            }
        }).fail(function(response) {
            if (response.status == 401) {
                check.textContent = '이메일을 입력하세요.';
                check.style = 'display: block; color: red';
                rb('email', email);
            }
        });
    },
    redBox : function(field, pre) {
        var box = document.getElementById(field);

        box.focus();
        box.style = 'border: 1px solid red; background-color: pink';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #ced4da';
            } else {
                box.style = 'border: 1px solid red; background-color: pink';
            }
        });
    },
    redBoxWof : function(field, pre) {
        var box = document.getElementById(field);

        box.style = 'border: 1px solid red; background-color: pink';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #ced4da';
            } else {
                box.style = 'border: 1px solid red; background-color: pink';
            }
        });
    }
};

main.init();