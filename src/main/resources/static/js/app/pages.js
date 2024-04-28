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
                _this.getOtherCommentPage(currentLocation, 1);

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
    getNewLocation: function(currentLocation, page) {
        if (currentLocation === '/') {
            currentLocation += 'main';
        } else if (currentLocation.includes('/content-report/details')) {
            var params = window.location.search;
            currentLocation += params.substring(0, params.indexOf('&'));
            return currentLocation + '&page=' + page;
        }
        return currentLocation + '?page=' + page;
    }
};

pagesMain.init();