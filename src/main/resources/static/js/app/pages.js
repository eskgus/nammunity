var pagesMain = {
    init: function() {
        var _this = this;

        var currentLocation = window.location.pathname;
        var isCurrentPagePostsRead = currentLocation.includes('/posts/read');
        if (!isCurrentPagePostsRead) {  // 나머지 화면
            $(document).on('click', '[name="page"]', function() {
                _this.getOtherPage(currentLocation, $(this).text());
            });
        } else {    // 글 읽기 화면
            // 화면 로드 시
            $(document).ready(function() {
                var hash = window.location.hash;
                if (hash) {   // 특정 댓글로 이동
                    _this.getSpecificCommentPage(currentLocation, hash);
                } else {
                    _this.getOtherCommentPage(currentLocation, 1);
                }

                // 댓글 페이지 선택 시
                $(document).on('click', '[name="page"]', function() {
                    _this.getOtherCommentPage(currentLocation, $(this).text());
                });
            });
        }
    },
    getOtherPage: function(currentLocation, page) {
        var newLocation = this.getNewLocation(currentLocation, page);
        window.location.href = newLocation;
    },
    getOtherCommentPage: function(currentLocation, page) {
        var newLocation = this.getNewLocation(currentLocation, page);
        $.ajax({
            type: 'GET',
            url: newLocation
        }).done(function(response) {
            $("#comments-area").html(response);
        });
    },
    getSpecificCommentPage: function(currentLocation, hash) {
        var commentId = hash.substring(hash.indexOf('-') + 1, hash.length);
        $.ajax({
            type: 'GET',
            url: currentLocation + '?cmt=' + commentId
        }).done(function(response) {
            $('#comments-area').html(response);
        });
    },
    getNewLocation: function(currentLocation, page) {
        if (currentLocation === '/') {
            currentLocation += 'main';
        } else if (currentLocation.includes('/content-report/details') || currentLocation.includes('/search')) {
            var params = new URLSearchParams(window.location.search);
            var pageParamIndex = params.get('page');

            if (pageParamIndex !== null) {
                params.delete('page');
            }

            params.set('page', page);
            return currentLocation + '?' + params;
        }
        return currentLocation + '?page=' + page;
    }
};

pagesMain.init();