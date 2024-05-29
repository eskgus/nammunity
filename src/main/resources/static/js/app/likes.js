import { indexMain } from './index.js';

var likesMain = {
    init: function() {
        var _this = this;

        // 게시글 좋아요
        var pLikeBtn = document.getElementById('btn-posts-like');
        pLikeBtn.addEventListener('click', function() {
            var post = {
                id: $('#id').val(),
                img: pLikeBtn.children[0].classList,
                sum: pLikeBtn.nextElementSibling
            };

            if (post.img[1] == 'like_s_d') {
                _this.deletePostLikes(post);
            } else {
                _this.savePostLikes(post);
            }
        });

        // 댓글 좋아요
        var cLikeBtns = document.getElementsByName('btn-cmts-like');
        for (let i = 0; i < cLikeBtns.length; i++) {
            cLikeBtns[i].addEventListener('click', function() {
                var divId = cLikeBtns[i].closest('.comment').parentElement.id;
                var cmt = {
                    id: divId.slice(divId.indexOf('-') + 1),
                    img: cLikeBtns[i].children[0].classList,
                    sum: cLikeBtns[i].nextElementSibling
                };

                if (cmt.img[1] == 'like_s_d') {
                    _this.deleteCmtLikes(cmt);
                } else {
                    _this.saveCmtLikes(cmt);
                }
            });
        }
    },
    savePostLikes: function(post) {
        $.ajax({
            type: 'POST',
            url: '/api/likes?postsId=' + post.id
        }).done(function(response) {
            post.img.add('like_s_d');
            post.sum.textContent = parseInt(post.sum.textContent) + 1;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    },
    deletePostLikes: function(post) {
        $.ajax({
            type: 'DELETE',
            url: '/api/likes?postsId=' + post.id
        }).done(function(response) {
            post.img.remove('like_s_d');
            post.sum.textContent = parseInt(post.sum.textContent) - 1;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    },
    saveCmtLikes: function(cmt) {
        $.ajax({
            type: 'POST',
            url: '/api/likes?commentsId=' + cmt.id
        }).done(function(response) {
            cmt.img.add('like_s_d');
            cmt.sum.textContent = parseInt(cmt.sum.textContent) + 1;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    },
    deleteCmtLikes: function(cmt) {
        $.ajax({
            type: 'DELETE',
            url: '/api/likes?commentsId=' + cmt.id
        }).done(function(response) {
            cmt.img.remove('like_s_d');
            cmt.sum.textContent = parseInt(cmt.sum.textContent) - 1;
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    }
};

likesMain.init();