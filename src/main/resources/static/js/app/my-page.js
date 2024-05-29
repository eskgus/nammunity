import { indexMain } from './index.js';

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
                var url = '/admin/my-page/content-report/details?' + result.type + '=' + result.id + '&page=1';

                // 팝업 열기
                _this.openReportDetailsPopup(url);
            });
        });

        // 전체 선택 체크박스 체크/해제
        $('#select-all').on('click', function() {
            $('table input[type="checkbox"]').prop('checked', this.checked);
        });

        // 개별 체크박스 체크 여부에 따라 전체 선택 체크박스 체크/해제
        $('table input[type="checkbox"]').on('change', function() {
            // 모든 개별 체크박스의 개수와 체크된 개별 체크박스의 개수 비교
            var allChecked = $('table input[type="checkbox"]').length == $('table input[type="checkbox"]:checked').length;
            $('#select-all').prop('checked', allChecked);
        });

        // 신고 내역 선택 삭제
        $('#btn-delete-reports').on('click', function() {
            var data = { postsId: [], commentsId: [], userId: [] };
            _this.deleteSelectedItems('reports', data);
        });

        // 작성 글 선택 삭제
        $('#btn-delete-posts').on('click', function() {
            var data = { postsId: [] };
            _this.deleteSelectedItems('posts', data);
        });

        // 작성 댓글 선택 삭제
        $('#btn-delete-comments').on('click', function() {
            var data = { commentsId: [] };
            _this.deleteSelectedItems('comments', data);
        });

        // 좋아요 선택 삭제
        $('#btn-delete-likes').on('click', function() {
            var data = { likesId: [] };
            _this.deleteSelectedItems('likes', data);
        });

        // 사용자 활동 정지
        $('#btn-user-ban').on('click', function() {
            var href = this.closest('.activity-history-area').querySelector('h2 a').href;
            var userId = href.slice(href.lastIndexOf('/') + 1);
            _this.banUser(userId);
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
            var href = $(closestUsers).find('a').attr('href');
            return { type: 'userId', id: href.slice(href.lastIndexOf('/') + 1) };
        }
    },
    getId: function(checkbox) {
        return checkbox.nextElementSibling.textContent;
    },
    deleteSelectedItems: function(type, data) {
        // 체크된 체크박스 가져오기
        var checkboxes = document.querySelectorAll('table input[type="checkbox"]:checked');
        // 체크된 체크박스가 없으면 알림 띄우기
        if (checkboxes.length == 0) {
            alert('삭제할 항목을 선택하세요.');
            return;
        }

        // 체크된 체크박스가 있으면 type에 따라 id 추출하는 함수 호출 + data에 저장
        checkboxes.forEach(checkbox => {
            if (type == 'reports') {
                var result = this.getIdByType(checkbox);
                data[result.type].push(result.id);
            } else {
                var result = this.getId(checkbox);
                var id = type + 'Id';
                data[id].push(result);
            }
        });

        // 삭제 요청 보내기
        var func = 'deleteSelected' + type.charAt(0).toUpperCase() + type.slice(1);
        this[func](data);
    },
    deleteSelectedReports: function(data) {
        if (confirm('정말로 삭제하시겠어요?')) {
            $.ajax({
                type: 'DELETE',
                url: '/api/reports/content/selected-delete',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data)
            }).done(function(response) {
                alert('삭제됐습니다.');
                window.location.reload();
            }).fail(function(xhRequest) {
                indexMain.fail(xhRequest);
            });
        }
    },
    deleteSelectedPosts: function(data) {
        if (confirm('정말로 삭제하시겠어요?')) {
            $.ajax({
                type: 'DELETE',
                url: '/api/posts/selected-delete',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data.postsId)
            }).done(function(response) {
                alert('삭제됐습니다.');
                window.location.reload();
            }).fail(function(xhRequest) {
                indexMain.fail(xhRequest);
            });
        }
    },
    deleteSelectedComments: function(data) {
        if (confirm('정말로 삭제하시겠어요?')) {
            $.ajax({
                type: 'DELETE',
                url: '/api/comments/selected-delete',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data.commentsId)
            }).done(function(response) {
                alert('삭제됐습니다.');
                window.location.reload();
            }).fail(function(xhRequest) {
                indexMain.fail(xhRequest);
            });
        }
    },
    deleteSelectedLikes: function(data) {
        if (confirm('정말로 삭제하시겠어요?')) {
            $.ajax({
                type: 'DELETE',
                url: '/api/likes/selected-delete',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data.likesId)
            }).done(function(response) {
                alert('삭제됐습니다.');
                window.location.reload();
            }).fail(function(xhRequest) {
                indexMain.fail(xhRequest);
            });
        }
    },
    banUser: function(userId) {
        var button = document.getElementById('btn-user-ban');
        button.disabled = true;

        $.ajax({
            type: 'POST',
            url: '/api/reports/process',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(userId)
        }).done(function(response) {
            alert('활동 정지 처리됐습니다.');
            window.location.reload();
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    }
};

reportDetailsMain.init();