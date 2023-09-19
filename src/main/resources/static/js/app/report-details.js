var reportDetailsMain = {
    init: function() {
        var _this = this;

        // 세부 신고 내역 팝업
        var reportDetails = document.getElementsByName('report-details');
        reportDetails.forEach(function(reportDetail) {
            reportDetail.addEventListener('click', function() {
                // 분류, id 추출
                var result = _this.getIdByType(reportDetail);

                // 추출한 분류, id로 세부 신고 내역 url 생성
                var url = '/admin/my-page/content-report/details?' + result.type + '=' + result.id;

                // 팝업 열기
                _this.openReportDetailsPopup(url);
            });
        });

        // 체크박스 선택
        $('#select-all').on('click', function() {
            $('table input[type="checkbox"]').prop('checked', this.checked);
        });

        // 신고 내역 선택 삭제
        $('#btn-delete-reports').on('click', function() {
            var checkboxes =  document.querySelectorAll('table input[type="checkbox"]');
            var data = { postsId: [], commentsId: [], userId: [] };

            // 선택된 항목의 분류, id 추출
            checkboxes.forEach(checkbox => {
                if (checkbox.checked == true) {
                    var result = _this.getIdByType(checkbox);
                    data[result.type].push(result.id);
                }
            });

            // 삭제 요청 보내기
            _this.deleteSelectedReports(data);
        });
    },
    openReportDetailsPopup: function(url) {
        var width = 800;
        var height = 450;
        var left = (window.innerWidth - width) / 2;
        var top = (window.innerHeight - height) / 2;

        window.open(url, '세부 신고 내역', 'width=' + width + ',height=' + height + ',left=' + left + ',top=' + top);
    },
    getIdByType: function(element) {
        var closestPosts = element.closest('.posts-area');
        var closestComments = element.closest('.comments-area');
        var closestUsers = element.closest('.users-area');

        if (closestPosts) {
            var post = closestPosts.querySelector('a');
            return { type: 'postsId', id: post.href.slice(post.href.lastIndexOf('/') + 1) };
        } else if (closestComments) {
            var comment = closestComments.querySelector('a');
            return { type: 'commentsId', id: comment.href.slice(comment.href.lastIndexOf('-') + 1) };
        } else {
            var user = closestUsers.children[1];
            return { type: 'userId', id: user.getAttribute('data-user-id') };
        }
    },
    deleteSelectedReports: function(data) {
        if (confirm('정말로 삭제하시겠어요?')) {
            var data = data;

            $.ajax({
                type: 'DELETE',
                url: '/api/reports/content',
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data)
            }).done(function(response) {
                alert(response[Object.keys(response)]);
                if (!response[Object.keys(response)].includes("항목")) {
                    window.location.reload();
                }
            }).fail(function(response) {
                alert(JSON.stringify(response));
            });
        }
    }
};

reportDetailsMain.init();