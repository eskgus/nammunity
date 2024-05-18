var indexMain = {
    init: function() {
        var _this = this;

        $('#btn-close-window').on('click', function() { // confirmEmail
            window.close();
        });

        $('#btn-back').on('click', function() { // 403
            window.history.back();
        });

        var searchBtns = document.getElementsByName('btn-search');
        searchBtns.forEach(function(searchBtn) {
            searchBtn.addEventListener('click', function() {
                _this.search(this);
            });
        });
    },
    redBox: function(field, pre) {
        var box = document.getElementById(field);

        box.style = 'border: 2px solid #ea3636; background-color: #ffc0cb';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #205943';
            } else {
                box.style = 'border: 2px solid #ea3636; background-color: #ffc0cb';
            }
        });
    },
    redBoxNBC: function(field, pre) {  // 배경 색 없는 빨간 박스
        var box = document.getElementById(field);

        box.style = 'border: 1px solid #ea3636';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: none';
            } else {
                box.style = 'border: 1px solid #ea3636';
            }
        });
    },
    redBoxWithoutBackgroundColour: function(field, rejectedValue) {
        console.log('red box without background colour...');
        var box = document.getElementById(field);

        var red = 'border: 1px solid #ea3636';
        box.style = red;
        box.addEventListener('input', function() {
            if (rejectedValue != box.value) {
                box.style = 'border: none';
            } else {
                box.style = red;
            }
        });
    },
    fail: function (response, data, rb) {  // ajax 응답 알림 창 띄우기
        console.log('indexMain.fail(response, data, redBox)...');
        var size = Object.keys(response).length;
        let firstData = 4;

        for (let i = 0; i < size; i++) {
            let error = Object.keys(response)[i];
            let index = Object.keys(data).indexOf(error);
            firstData = firstData > index ? index : firstData;
            rb(error, data[Object.keys(data)[index]]);
        }

        var firstError = Object.keys(data)[firstData];
        alert(response[firstError]);
        document.getElementById(Object.keys(data)[firstData]).focus();
    },
    fail: function(xhRequest, validExceptionHandler) {  // 요청 실패 시 처리
        if (xhRequest.status === 400) {
            var errors = xhRequest.responseJSON;
            if (Array.isArray(errors) && errors.length > 0) {   // MethodArgumentNotValidException
                validExceptionHandler(errors);
            } else {    // IllegalArgumentException
                alert(xhRequest.responseText);
                window.location.reload();
            }
        } else {
            alert(xhRequest.responseText);
            switch (xhRequest.status) {
                case 401:
                    window.location.href = '/users/sign-in';
                    break;
                case 403:
                    window.location.href = '/';
                    break;
                default:    // Exception
                    window.location.reload();
                    break;
            }
        }
    },
    handleValidException: function(errors, prefix, suffix) {
        for (var i = 0; i < errors.length; i++) {
            var error = errors[i];
            this.redBoxWithoutBackgroundColour(prefix + error.field + suffix, error.rejectedValue);
        }
        var firstError = errors[0];
        alert(firstError.defaultMessage);
        document.getElementById(prefix + firstError.field + suffix).focus();
    },
    search: function(searchBtn) {
        var keywords = searchBtn.previousElementSibling.value;
        var currentLocation = window.location.href;
        var newLocation;

        // 게시글/댓글/사용자 검색 결과 화면에서 검색한 게 아니면 전체 검색 결과로 이동
        if (currentLocation.includes('/search/posts') || currentLocation.includes('/search/comments')
            || currentLocation.includes('/search/users')) {
            var url1 = currentLocation.slice(0, currentLocation.indexOf('=') + 1);
            var url2 = '';
            if (currentLocation.includes('&')) {
                url2 = currentLocation.slice(currentLocation.indexOf('&'), currentLocation.length);
            }
            newLocation = url1 + keywords + url2;
        } else {
            newLocation = '/search?keywords=' + keywords;
        }

        window.location.href = newLocation;
    }
};

indexMain.init();

export { indexMain };