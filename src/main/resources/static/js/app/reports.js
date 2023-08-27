var reportsMain = {
    popupContainer: document.getElementById('report-popup-container'),
    reasons: document.querySelectorAll('#report-popup input[type="checkbox"]'),
    otherReasons: document.getElementById('otherReasons'),
    init: function() {
        var _this = this;

        // 신고 팝업 열기
        $('#btn-report').on('click', function() {
            _this.openReportPopup();
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
                _this.reportPosts(reasonsId);
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
    reportPosts: function(reasonsId) {
        var data = {
            postsId: $('#id').val(),
            reasonsId: reasonsId,
            otherReasons: reportsMain.otherReasons.value
        };

        if (data.otherReasons == '') {
            data.otherReasons = null;
        }
        if (data.reasonsId == 0) {
            data.reasonsId = null;
        }

        $.ajax({
            type: 'POST',
            url: '/api/reports/community',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function(response) {
            if (Object.keys(response) == 'OK') {
                alert(response[Object.keys(response)]);
                reportsMain.closeReportPopup();
            } else {
                alert(response[Object.keys(response)]);
                if (response[Object.keys(response)].includes('ID')) { // reporter 없는 경우
                    window.location.href = '/users/sign-out';
                } else if (response[Object.keys(response)].includes('게시글')) { // 게시글 없는 경우
                    window.location.href = '/';
                }
            }
        }).fail(function(response) {
            if (response.status == 401) {
                alert('로그인하세요.');
                reportsMain.closeReportPopup();
            } else {
                alert(JSON.stringify(response));
            }
        });
    }
};

reportsMain.init();