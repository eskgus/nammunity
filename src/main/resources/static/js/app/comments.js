var commentsMain = {
    // event 함수에서 쓸 변수 초기화
    e: false,
    di: null,
    re: null,
    rdi: null,

    init: function() {
        var _this = this;

        // 댓글 저장
        document.getElementById('btn-cmt-save').addEventListener('click', function() {
            _this.saveComments();
        });

        // 댓글 수정
        _this.event('edit', _this.editComments);

        // 댓글 삭제
        _this.event('delete', _this.deleteComments);

        $(document).ready(function() {
            var commentId = window.location.hash;
            if (commentId) {    // commentId가 존재하는 경우 해당 댓글 위치로 스크롤 이동
                console.log('commentId is present');
                _this.focusOnTargetComment(commentId);
            }
        });

        _this.loadUserHistory();    // 사용자 활동 내역
    },
    saveComments: function() {
        var data = {
            content: $('#cmt-content').val(),
            postsId: $('#id').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/comments',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                window.location.reload();
            } else {
                alert(response[Object.keys(response)]);
                if (Object.keys(response) == 'error') {
                    window.location.href = '/';
                }
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });
    },
    event: function(name, func) {
        var btns = document.getElementsByName('btn-cmt-' + name);
        for (let i = 0; i < btns.length; i++) {
            btns[i].addEventListener('click', function() {
                if (commentsMain.e == true && (func.name.includes('edit') || func.name.includes('delete'))) {
                    commentsMain.cancelEditComments(commentsMain.di);
                }

                if (func.name.includes('edit') || func.name.includes('delete')) {
                    [commentsMain.re, commentsMain.rdi] = func(btns[i]);
                } else {
                    [commentsMain.re, commentsMain.rdi] = func(commentsMain.di);
                }

                if (commentsMain.re == 'e') {
                    commentsMain.e = true;
                } else {
                    commentsMain.e = false;
                }
                commentsMain.di = commentsMain.rdi;
            });
        }
    },
    editComments: function(btn) {
        var divId = $(btn)[0].href.slice($(btn)[0].href.indexOf('#') + 1);
        var div = document.getElementById(divId);

        var cmt = div.children[0];
        var data = {
            author: cmt.children[0].innerHTML,
            content: cmt.children[2].innerHTML,
            date: cmt.children[3].innerHTML,
            id: divId.slice(divId.indexOf('-') + 1)
        };

        cmt.style = 'display: none';

        var html = '<div class="comments-creation">'
                    + '<span class="author">' + data.author + '</span>'
                    + '<textarea class="cmt-content" id="cmt-content-' + data.id + '" rows="4">' + data.content + '</textarea>'
                    + '<div class="btn-area">'
                    + '<button type="button" class="btn" name="btn-cmt-update">수정</button>'
                    + '<button type="button" class="btn" name="btn-cmt-cancel">취소</button>'
                    + '</div></div>';
        div.insertAdjacentHTML('beforeend', html);

        commentsMain.event('update', commentsMain.updateComments);
        commentsMain.event('cancel', commentsMain.cancelEditComments);

        return ['e', divId];
    },
    updateComments: function(divId) {
        var id = divId.slice(divId.indexOf('-') + 1);
        var data = {
            content: document.getElementById(divId).children[1].children[1].value
        };

        $.ajax({
            type: 'PUT',
            url: '/api/comments/' + id,
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                window.location.reload();
            } else {
                alert(response[Object.keys(response)]);
                if (Object.keys(response) == 'error') {
                    window.location.reload();
                }
            }
        }).fail(function(response) {
            alert(JSON.stringify(response));
        });

        return ['u', divId];
    },
    cancelEditComments: function(divId) {
        var div = document.getElementById(divId);

        div.children[0].style = 'display: block';
        div.children[1].remove();

        return ['c', null];
    },
    deleteComments: function(btn) {
        var divId;
        if (confirm('정말로 삭제하시겠어요?')) {
            divId = btn.closest('.comment').parentElement.id;
            var id = divId.slice(divId.indexOf('-') + 1);

            $.ajax({
                type: 'DELETE',
                url: '/api/comments/' + id,
                contentType: 'application/json; charset=utf-8'
            }).done(function(response) {
                alert(response[Object.keys(response)]);
                window.location.reload();
            }).fail(function(response) {
                alert(JSON.stringify(response));
            });
        }
        return ['d', divId];
    },
    focusOnTargetComment: function(commentId) {
        var $targetComment = $(commentId);
        if ($targetComment.length) {
            $('html, body').animate({
                scrollTop: $targetComment.offset().top
            }, 1000);
        }
    },
    loadUserHistory: function() {
        var loadUserHistoryBtns = document.getElementsByName('btn-history');
        loadUserHistoryBtns.forEach(function(loadUserHistoryBtn) {
            loadUserHistoryBtn.addEventListener('click', function() {
                window.location.href = this.href;
            });
        });
    }
};

commentsMain.init();