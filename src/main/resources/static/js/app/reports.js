import { indexMain } from './index.js';

var reportsMain = {
    popupContainer: document.getElementById('report-popup-container'),
    reasons: document.querySelectorAll('#report-popup input[type="checkbox"]'),
    otherReasons: document.getElementById('otherReasons'),
    id: document.getElementById('reportId'),
    init: function() {
        var _this = this;

        // 신고 팝업 열기
        var openPopupBtns = document.getElementsByName('btn-report');
        openPopupBtns.forEach(function(openPopupBtn) {
            openPopupBtn.addEventListener('click', function() {
                var postsReportBtn = document.getElementById('btn-posts-report');
                var cmtReportBtn = document.getElementById('btn-cmt-report');
                var userReportBtn = document.getElementById('btn-user-report');

                // 게시글/댓글/사용자 신고 버튼 초기화
                postsReportBtn.style.display = 'none';
                cmtReportBtn.style.display = 'none';
                userReportBtn.style.display = 'none';

                var closestPosts = this.closest('.posts-area');
                var closestComments = this.closest('.comment');
                var closestUsers = this.closest('.author');

                if (closestUsers) { // 사용자 신고 버튼이면 신고 팝업 내부의 사용자 신고 버튼 표시 + 신고할 컨텐츠 id 변경
                    var href = $(closestUsers).find('a[name="btn-history"]').attr('href');
                    _this.id.value = href.slice(href.lastIndexOf('/') + 1);
                    userReportBtn.style.display = 'inline-block';
                } else if (closestPosts) { // 게시글의 신고 버튼이면 신고 팝업 내부의 게시글 신고 버튼 표시 + 신고할 컨텐츠 id 변경
                    _this.id.value = $('#id').val();
                    postsReportBtn.style.display = 'inline-block';
                } else if (closestComments) {   // 댓글의 신고 버튼이면 신고 팝업 내부의 댓글 신고 버튼 표시 + 신고할 컨텐츠 id 변경
                    var divId = closestComments.parentElement.id;
                    _this.id.value = divId.slice(divId.indexOf('-') + 1);
                    cmtReportBtn.style.display = 'inline-block';
                } else {
                    alert('신고할 컨텐츠가 선택되지 않았습니다.');
                }

                _this.openReportPopup();
            });
        });

        // 신고 팝업 닫기 (취소 버튼)
        $('#btn-report-cancel').on('click', function() {
            _this.closeReportPopup();
        });

        // (팝업이 아닌 구역 클릭)
        _this.popupContainer.addEventListener('click', (event) => {
            if (event.target === _this.popupContainer) {
                _this.closeReportPopup();
            }
        });

        // 신고 사유 선택
        _this.reasons.forEach((reason) => {
            reason.addEventListener('change', () => {
                // 한 번에 한 개씩만 선택 가능
                _this.reasons.forEach((r) => {
                    r.checked = false;
                });
                reason.checked = true;

                // 선택된 사유가 기타면 otherReasons 입력 박스 표시
                if (_this.reasons[_this.reasons.length - 1] === reason) {
                    _this.otherReasons.style.display = 'block';
                } else {
                    _this.otherReasons.style.display = 'none';
                }
            });
        });

        // 게시글 신고
        $('#btn-posts-report').on('click', function() {
            var reasonsId = _this.getReasons();
            if (reasonsId != null) {
                _this.saveContentReports('posts', reasonsId);
            }
        });

        // 댓글 신고
        $('#btn-cmt-report').on('click', function() {
            var reasonsId = _this.getReasons();
            if (reasonsId != null) {
                _this.saveContentReports('comments', reasonsId);
            }
        });

        // 사용자 신고
        $('#btn-user-report').on('click', function() {
            var reasonsId = _this.getReasons();
            if (reasonsId != null) {
                _this.saveContentReports('users', reasonsId);
            }
        });
    },
    openReportPopup: function() {
        // 팝업 열기 전에 체크 박스 초기화
        reportsMain.reasons.forEach((reason) => {
            reason.checked = false;
        });
        reportsMain.otherReasons.value = '';
        reportsMain.otherReasons.style.display = 'none';

        reportsMain.popupContainer.style.display = 'block';
    },
    closeReportPopup: function() {
        reportsMain.popupContainer.style.display = 'none';
    },
    getReasons: function() {    // 신고 사유 반환
        let reasonsId = 0;
        reportsMain.reasons.forEach((reason, i) => {
            // reasonsId
            if (reason.checked) {
                reasonsId = i + 1;
            }
        });

        if (reasonsId === 0) {
            alert('신고 사유를 선택하세요.');
            return null;
        } else if (reasonsId === reportsMain.reasons.length && reportsMain.otherReasons.value === '') {
            alert('기타 사유를 입력하세요.');
            return null;
        }

        return reasonsId;
    },
    saveContentReports: function(type, reasonsId) {
        var data = this.generateData(type, reasonsId);

        $.ajax({
            type: 'POST',
            url: '/api/reports/content',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            alert('신고됐습니다.');
            reportsMain.closeReportPopup();
        }).fail(function(xhRequest) {
            indexMain.fail(xhRequest);
        });
    },
    generateData: function(type, reasonsId) {
        var contentId = this.id.value;
        var otherReasons = this.otherReasons.value;

        var data = {
            postsId: type === 'posts' ? contentId : null,
            commentsId: type === 'comments' ? contentId : null,
            userId: type === 'users' ? contentId : null,
            reasonsId: reasonsId !== 0 ? reasonsId : null,
            otherReasons: otherReasons !== '' ? otherReasons : null
        };

        return data;
    }
};

reportsMain.init();