var pLikeBtn =  document.getElementById('btn-posts-like');
pLikeBtn.addEventListener('click', function() {
    var post = {
        id: $('#id').val(),
        img: pLikeBtn.children[0].classList,
        sum: pLikeBtn.nextElementSibling
    };

    if (post.img[1] == 'like_s_d') {
        deletePostLikes(post);
    } else {
        savePostLikes(post);
    }
});

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
            deleteCmtLikes(cmt);
        } else {
            saveCmtLikes(cmt);
        }
    });
}

function savePostLikes(post) {
    $.ajax({
        type: 'POST',
        url: '/api/likes?postsId=' + post.id
    }).done(function(response) {
        if (Object.keys(response) == 'OK') {
            post.img.add('like_s_d');
            post.sum.textContent = parseInt(post.sum.textContent) + 1;
        } else {
            alert(response[Object.keys(response)]);
            window.location.href = '/';
        }
    }).fail(function(response) {
        if (response.status == 401) {
            alert('로그인하세요.');
        } else {
            alert(JSON.stringify(response));
        }
    });
}

function deletePostLikes(post) {
    $.ajax({
        type: 'DELETE',
        url: '/api/likes?postsId=' + post.id
    }).done(function(response) {
        if (response == 'OK') {
            post.img.remove('like_s_d');
            post.sum.textContent = parseInt(post.sum.textContent) - 1;
        } else {
            alert(response);
            window.location.href = '/';
        }
    }).fail(function(response) {
        alert(JSON.stringify(response));
    });
}

function saveCmtLikes(cmt) {
    $.ajax({
        type: 'POST',
        url: '/api/likes?commentsId=' + cmt.id
    }).done(function(response) {
        if (Object.keys(response) == 'OK') {
            cmt.img.add('like_s_d');
            cmt.sum.textContent = parseInt(cmt.sum.textContent) + 1;
        } else {
            alert(response[Object.keys(response)]);
            window.location.href = '/';
        }
    }).fail(function(response) {
        if (response.status == 401) {
            alert('로그인하세요.');
        } else {
            alert(JSON.stringify(response));
        }
    });
}

function deleteCmtLikes(cmt) {
    $.ajax({
        type: 'DELETE',
        url: '/api/likes?commentsId=' + cmt.id
    }).done(function(response) {
        if (response == 'OK') {
            cmt.img.remove('like_s_d');
            cmt.sum.textContent = parseInt(cmt.sum.textContent) - 1;
        } else {
            alert(response);
            window.location.href = '/';
        }
    }).fail(function(response) {
        alert(JSON.stringify(response));
    });
}
