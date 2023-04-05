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
        }) ;
    },
    signUp : function() {
        var data = {
            username: $('#username').val(),
            password: $('#password').val(),
            nickname: $('#nickname').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/user',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data),
        }).done(function(response) {
            alert('회원가입이 완료됐습니다.');
            window.location.href = '/';
        }).fail(function(response) {
            if (JSON.stringify(response.responseJSON).includes('error')) {
                alert(JSON.stringify(response.responseJSON.error).replaceAll("\"", ""));
            } else {
                if (JSON.stringify(response.responseJSON).includes("username")) {
                    alert(JSON.stringify(response.responseJSON.username).replaceAll("\"", ""));
                    $('#username').focus();
                } else if (JSON.stringify(response.responseJSON).includes("password")) {
                    alert(JSON.stringify(response.responseJSON.password).replaceAll("\"", ""));
                    $('#password').focus();
                } else if (JSON.stringify(response.responseJSON).includes("nickname")) {
                    alert(JSON.stringify(response.responseJSON.nickname).replaceAll("\"", ""));
                    $('#nickname').focus();
                }
            }
        });
    },
    checkUsername : function() {
        var username = $('#username').val();
        var dupl = document.getElementById('ch-dupl-username');

        $.ajax({
            type : 'GET',
            url : '/api/exists/username/' + username,
            success : function(response){
                if (response == false) {
                    dupl.style = 'display: none';
                    $('#username').css('border', '1px solid black');
                } else {
                    dupl.textContent = '이미 사용 중인 ID입니다.';
                    dupl.style = 'display: block; color: red';
                    $('#username').css('border', '1px solid red');
                }
            }
        });
    },
    checkNickname : function() {
        var nickname = $('#nickname').val();
        var dupl = document.getElementById('ch-dupl-nickname');

        $.ajax({
            type: 'GET',
            url: '/api/exists/nickname/' + nickname,
            success : function(response) {
                if (response == false) {
                    dupl.style = 'display: none';
                    $('#nickname').css('border', '1px solid black');
                } else {
                    dupl.textContent = '이미 사용 중인 닉네임입니다.';
                    dupl.style = 'display: block; color: red';
                    $('#nickname').css('border', '1px solid red');
                }
            }
        });
    }
};

main.init();