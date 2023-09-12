var reportDetailsMain = {
    init: function() {
        var _this = this;

        // 세부 신고 내역 팝업
        var reportDetails = document.getElementsByName('report-details');
        reportDetails.forEach(function(reportDetail) {
            reportDetail.addEventListener('click', function() {
                // 신고 분류 판별
                var report = reportDetail.closest('tr');
                var type = report.firstElementChild.textContent;
                var href = null;
                var url = '/users/my-page/content-report/details?';

                if (type == '게시글') {
                    // 게시글 세부 신고 내역 url 생성
                    href = report.children[1].firstElementChild.firstElementChild.href;
                    var postsId = href.slice(href.lastIndexOf('/') + 1);
                    url += 'postId=' + postsId;
                } else if (type == '댓글') {
                    // 댓글 세부 신고 내역 url 생성
                    href = report.children[1].firstElementChild.href;
                    var commentsId = href.slice(href.lastIndexOf('-') + 1);
                    url += 'commentId=' + commentsId;
                } else {
                    // 사용자 세부 신고 내역 url 생성
                    var userId = report.children[1].getAttribute('data-user-id');
                    url += 'userId=' + userId;
                }

                // 팝업 열기
                _this.openReportDetailsPopup(url);
            });
        });
    },
    openReportDetailsPopup: function(url) {
        var width = 800;
        var height = 450;
        var left = (window.innerWidth - width) / 2;
        var top = (window.innerHeight - height) / 2;

        window.open(url, '세부 신고 내역', 'width=' + width + ',height=' + height + ',left=' + left + ',top=' + top);
    }
};

reportDetailsMain.init();