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
            nickname: $('#nickname').val()
        };

        var rb = this.redBox;

        $.ajax({
            type: 'POST',
            url: '/api/user',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data),
        }).done(function() {
            alert('회원가입이 완료됐습니다.');
            window.location.href = '/';
        }).fail(function(response) {
            if (JSON.stringify(response.responseJSON).includes('exist')) {
                if (JSON.stringify(response.responseJSON.existingUsername)) {
                    alert(JSON.stringify(response.responseJSON.existingUsername).replaceAll('\"', ''));
                    rb('username', data.username);
                } else {
                    alert(JSON.stringify(response.responseJSON.existingNickname).replaceAll('\"', ''));
                    rb('nickname', data.nickname);
                }
            } else {
                if (JSON.stringify(response.responseJSON).includes('username')) {
                    alert(JSON.stringify(response.responseJSON.username).replaceAll('\"', ''));
                    rb('username', data.username);
                } else if (JSON.stringify(response.responseJSON).includes('password')) {
                    alert(JSON.stringify(response.responseJSON.password).replaceAll('\"', ''));
                    rb('password', data.password);
                } else if (JSON.stringify(response.responseJSON).includes('nickname')) {
                    alert(JSON.stringify(response.responseJSON.nickname).replaceAll('\"', ''));
                    rb('nickname', data.nickname);
                }
            }
        });
    },
    checkUsername : function() {
        var username = $('#username').val();
        var dupl = document.getElementById('ch-dupl-username');
        var rb = this.redBoxWof;

        $.ajax({
            type: 'GET',
            url: '/api/exists/username/' + username
        }).done(function(response) {
            if (response == false) {
                dupl.style = 'display: none';
            } else {
                dupl.textContent = '이미 사용 중인 ID입니다.';
                dupl.style = 'display: block; color: red';
                rb('username', username);
            }
        }).fail(function(response) {
            if (JSON.stringify(response.responseJSON.status) == 404) {
                dupl.textContent = 'ID를 입력하세요.';
                dupl.style = 'display: block; color: red';
                rb('username', username);
            }
        });
    },
    checkNickname : function() {
        var nickname = $('#nickname').val();
        var dupl = document.getElementById('ch-dupl-nickname');
        var rb = this.redBoxWof;

        $.ajax({
            type: 'GET',
            url: '/api/exists/nickname/' + nickname
        }).done(function(response) {
            if (response == false) {
                dupl.style = 'display: none';
            } else {
                dupl.textContent = '이미 사용 중인 닉네임입니다.';
                dupl.style = 'display: block; color: red';
                rb('nickname', nickname);
            }
        }).fail(function(response) {
            if (JSON.stringify(response.responseJSON.status) == 404) {
                dupl.textContent = '닉네임을 입력하세요.';
                dupl.style = 'display: block; color: red';
                rb('nickname', nickname);
            }
        });
    },
    checkPassword : function() {
        var password = $('#password').val();
        var blank = document.getElementById('ch-blank-password');
        var rb = this.redBoxWof;

        if (password == '') {
            blank.textContent = '비밀번호를 입력하세요.';
            blank.style = 'display: block; color: red';
            rb('password', password);
        } else {
            blank.style = 'display: none';
        }
    },
    redBox : function(field, pre) {
        var box = document.getElementById(field);
        box.focus();
        box.style = 'border: 1px solid red';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid black';
            } else {
                box.style = 'border: 1px solid red';
            }
        });
    },
    redBoxWof : function(field, pre) {
        var box = document.getElementById(field);
        box.style = 'border: 1px solid red';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid black';
            } else {
                box.style = 'border: 1px solid red';
            }
        });
    }
};

main.init();