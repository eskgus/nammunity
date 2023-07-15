document.getElementById('btn-cmt-save').addEventListener('click', function() {
    saveComments();
});

var e = false;
var di;
var [re, rdi] = [,];
event('edit', edit);
event('delete', deleteComments);

function saveComments() {
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
}

function event(name, func) {
    var btns = document.getElementsByName('btn-cmt-' + name);
    for (let i = 0; i < btns.length; i++) {
        btns[i].addEventListener('click', function() {
            if (e == true && (func.name == 'edit' || func.name.includes('delete'))) {
                cancel(di);
            }

            if (func.name == 'edit' || func.name.includes('delete')) {
                [re, rdi] = func(btns[i]);
            } else {
                [re, rdi] = func(di);
            }

            if (re == 'e') {
                e = true;
            } else {
                e = false;
            }
            di = rdi;
        });
    }
}

function edit(btn) {
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

    event('update', update);
    event('cancel', cancel);

    return ['e', divId];
}

function update(divId) {
    var id =  divId.slice(divId.indexOf('-') + 1);
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
}

function cancel(divId) {
    var div = document.getElementById(divId);

    div.children[0].style = 'display: block';
    div.children[1].remove();

    return ['c', null];
}

function deleteComments(btn) {
    var divId;
    if (confirm('정말로 삭제하시겠어요?')) {
        var href = btn.parentElement.previousElementSibling.children[0].href;
        divId = href.slice(href.indexOf('#') + 1);
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
}
