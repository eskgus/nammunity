var indexMain = {
    init: function() {
        var _this = this;

        $('#btn-close-window').on('click', function() { // confirmEmail
            window.close();
        });

        $('#btn-back').on('click', function() { // mvc controller IllegalArgumentException
            window.location.href = '/';
        });

        var searchBtns = document.getElementsByName('btn-search');
        searchBtns.forEach(function(searchBtn) {
            searchBtn.addEventListener('click', function() {
                _this.search(this);
            });
        });
    },
    fail: function(xhRequest, validExceptionHandler = null) {  // rest controller 요청 실패 시 처리
        if (xhRequest.status === 400) {
            var errors = xhRequest.responseJSON;
            if (Array.isArray(errors) && errors.length > 0) {   // MethodArgumentNotValidException, CustomValidException
                validExceptionHandler(errors);
            } else {    // IllegalArgumentException
                alert(xhRequest.responseText);
                window.location.reload();
            }
        } else {
            this.notBadRequestError(xhRequest);
        }
    },
    handleValidException: function(errors, useRedBorder = false, prefix = '', suffix = '') {
        for (var i = 0; i < errors.length; i++) {
            var error = errors[i];
            var field = prefix + error.field + suffix;

            if (useRedBorder) {
                indexMain.redBorder(field, error.rejectedValue);
            } else {
                indexMain.redBox(field, error.rejectedValue);
            }
        }
        var firstError = errors[0];
        alert(firstError.defaultMessage);
        document.getElementById(prefix + firstError.field + suffix).focus();
    },
    redBorder: function(field, rejectedValue) {
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
    redBox: function(field, rejectedValue) {
        var box = document.getElementById(field);

        var red = 'border: 2px solid #ea3636; background-color: #ffc0cb';
        var green = 'border: 1px solid #205943';

        box.style = red;
        box.addEventListener('input', function() {
            if (rejectedValue != box.value) {
                box.style = green;
            } else {
                box.style = red;
            }
        });
    },
    notBadRequestError: function(xhRequest) {
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