import { indexMain } from './index.js';

var postsMain = {
    init: function() {
        var _this = this;

        // 게시글 저장
        $('#btn-save').on('click', function() {
            _this.savePosts();
        });

        // 게시글 수정
        $('#btn-update').on('click', function() {
            _this.updatePosts();
        });

        // 게시글 삭제
        $('#btn-delete').on('click', function() {
            _this.deletePosts();
        });
    },
    savePosts: function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/posts',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('저장됐습니다.');
            window.location.href = '/';
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, (errors) => {
                indexMain.handleValidException(errors, '', '');
            });
        });
    },
    updatePosts: function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();

        $.ajax({
            type: 'PUT',
            url: '/api/posts/' + id,
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('수정됐습니다.');
            window.location.href = '/posts/read/' + id;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest, (errors) => {
                indexMain.handleValidException(errors, '', '');
            });
        });
    },
    deletePosts: function() {
        if (confirm('정말로 삭제하시겠어요?')) {
            var id = $('#id').val();

            $.ajax({
                type: 'DELETE',
                url: '/api/posts/' + id,
                contentType: 'application/json; charset=utf-8'
            }).done(function(response) {
                alert('삭제됐습니다.');
                window.location.href = '/';
            }).fail(function(xhRequest) {
                indexMain.fail(xhRequest, null);
            });
        }
    }
};

postsMain.init();