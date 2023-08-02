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
