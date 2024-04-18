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

        // 사용자 활동 내역
        var loadUserHistoryBtns = document.getElementsByName('btn-history');
        loadUserHistoryBtns.forEach(function(loadUserHistoryBtn) {
            loadUserHistoryBtn.addEventListener('click', function() {
                window.location.href = this.href;
            });
        });

        if (window.location.pathname.includes('/posts/read')) {
            // 글 읽기 화면 로드 시 댓글 가져오기
            $(document).ready(function() {
                _this.readComments(1);

                // 글 읽기 화면에서 댓글 페이지 선택 시 댓글 가져오기
                $(document).on('click', '[name="page"]', function() {
                    _this.readComments($(this).text());
                });
            });
        }

        if ((window.location.pathname === '/') || (window.location.pathname.includes('/main'))) {
            $(document).on('click', '[name="page"]', function() {
                _this.getMainPage($(this).text());
            })
        }
    },
    savePosts: function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var rb = indexMain.redBoxNBC;
        var fail = indexMain.fail;

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
    updatePosts: function() {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        var id = $('#id').val();
        var rb = indexMain.redBoxNBC;
        var fail = indexMain.fail;

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
            } else if (Object.keys(response) == 'error') {  // 게시글이 없는 경우
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
    deletePosts: function() {
        if (confirm('정말로 삭제하시겠어요?')) {
            var id = $('#id').val();

            $.ajax({
                type: 'DELETE',
                url: '/api/posts/' + id,
                contentType: 'application/json; charset=utf-8'
            }).done(function(response) {
                alert(response[Object.keys(response)]);
                window.location.href = '/';
            }).fail(function(response) {
                if (response.status == 403) {
                    alert('권한이 없습니다.');
                    window.history.back();
                } else {
                    alert(JSON.stringify(response));
                }
            });
        }
    },
    readComments: function(page) {
        var id = $('#id').val();
        $.ajax({
            type: 'GET',
            url: '/posts/read/' + id + '?page=' + page
        }).done(function(response) {
            $("#comments-area").html(response);
        });
    },
    getMainPage: function(page) {
        var location = window.location.pathname;
        if (location === '/') {
            location += 'main';
        }
        window.location.href = location + '?page=' + page;
    }
};

postsMain.init();