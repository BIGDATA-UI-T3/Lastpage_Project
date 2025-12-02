document.addEventListener("DOMContentLoaded", function () {

    // --- 1. AOS 초기화 ---
    if (typeof AOS !== 'undefined') {
        AOS.init({
            once: false, // 반복 실행
            threshold: 0.3,
            duration: 800
        });
    }

    // --- 2. 헤더 스크롤 이벤트 ---
    const header = document.querySelector('header');
    if (header) {
        window.addEventListener('scroll', () => {
            header.style.top = (window.scrollY > 120) ? '-80px' : '0';
        });
    }

    // --- 3. 지도 로직 시작 ---

    // HTML에서 전역 변수로 넘겨준 데이터 가져오기
    const PLACES = window.PLACES_DATA || [];

    const mapEl = document.getElementById("map");
    const placeListEl = document.getElementById("placeList");
    const searchInput = document.getElementById("searchInput");
    const searchButton = document.getElementById("searchButton");

    // 유틸리티 함수
    const $ = s => document.querySelector(s);
    const $$ = s => Array.from(document.querySelectorAll(s));

    // 네이버 지도 로드 확인
    if (!window.naver || !window.naver.maps) {
        console.error("Naver Maps SDK가 로드되지 않았습니다.");
        if (mapEl) mapEl.innerHTML = "<div style='text-align:center; padding: 20px;'>지도를 불러오는 데 실패했습니다.<br>NCP 클라이언트 ID를 확인해주세요.</div>";
        if (searchInput) searchInput.placeholder = "지도 로드 실패";
        return;
    }

    // 지도 옵션 설정
    const mapOptions = {
        center: new naver.maps.LatLng(36.8714, 127.6014), // 대한민국 중심
        zoom: 8,
        minZoom: 7,
        draggable: true,
        scrollWheel: true,
        disableDoubleClickZoom: false,
        disableDoubleTapZoom: false,
        pinchZoom: true,
        keyboardShortcuts: true,
    };
    const map = new naver.maps.Map("map", mapOptions);

    const markers = {};
    const placeCards = {};
    const infoWindow = new naver.maps.InfoWindow({
        content: '',
        maxWidth: 300,
        backgroundColor: "#fff",
        borderColor: "#e0d8cc",
        borderWidth: 1,
        anchorSize: new naver.maps.Size(10, 10),
        anchorSkew: true,
        pixelOffset: new naver.maps.Point(20, -20)
    });

    // --- 핵심 함수 정의 ---

    // 1. 목록 카드 HTML 생성
    function createPlaceCardElement(place) {
        const servicesHTML = (place.services || [])
            .map(s => `<span class="chip">${s}</span>`).join('');

        const encodedPlaceName = encodeURIComponent(place.name);
        const reserveUrl = `/reserve/funeral_reserve?placeName=${encodedPlaceName}`;

        const cardDiv = document.createElement('div');
        cardDiv.className = 'place-card';
        cardDiv.dataset.id = place.id;
        cardDiv.innerHTML = `
            <h3>${place.name}</h3>
            <p class="addr">${place.address}</p>
            <p class="phone">${place.phone}</p>
            <div class="chips">${servicesHTML}</div>
            <div class="links">
                <a href="#" class="review-link">후기 보기</a>
                <a href="${place.homepageUrl || '#'}" target="_blank" rel="noopener noreferrer">홈페이지</a>
                <a href="${reserveUrl}" class="btn-reserve">예약하기</a>
            </div>
        `;

        // 후기 보기 버튼 이벤트 (href="#" 방지)
        const reviewLink = cardDiv.querySelector('.review-link');
        if(reviewLink) {
            reviewLink.addEventListener('click', function(e) {
                e.preventDefault();
                alert('후기 기능 준비 중입니다.');
            });
        }

        cardDiv.addEventListener("click", function (e) {
            // A 태그(링크)를 클릭한 게 아니면 지도 이동
            if (e.target.tagName !== 'A') {
                const id = cardDiv.dataset.id;
                const marker = markers[id];
                if (marker) {
                    // 이동과 줌을 동시에 부드럽게 처리
                    map.morph(marker.getPosition(), 12);
                    // naver.maps.Event.trigger(marker, 'click'); // 필요시 정보창 열기
                    highlightActiveCard(id);
                }
            }
        });
        return cardDiv;
    }

    // 2. 정보창 내용 HTML 생성
    function createInfoWindowContent(place) {
        return `
            <div style="padding:10px; min-width:200px; line-height:1.6;">
                <h5 style="margin:0 0 5px; font-weight:600; color: #2C3930;">${place.name}</h5>
                <p style="font-size:13px; margin:0;">${place.address}</p>
                <p style="font-size:13px; margin:0; color:#a2725c; font-weight: 500;">${place.phone}</p>
                <a href="${place.homepageUrl || '#'}" target="_blank" rel="noopener noreferrer" style="font-size:13px; color:#007bff; text-decoration:none;">홈페이지 방문</a>
            </div>
        `;
    }

    // 3. (Promise) 주소 -> 좌표 변환
    function geocodeAddress(place) {
        return new Promise((resolve, reject) => {
            if (place.latitude && place.longitude) {
                place.coords = new naver.maps.LatLng(place.latitude, place.longitude);
                resolve(place);
                return;
            }

            naver.maps.Service.geocode({ query: place.address }, (status, response) => {
                if (status !== naver.maps.Service.Status.OK || !response.v2.addresses.length) {
                    console.warn(`[${place.name}] 지오코딩 실패: ${place.address}`);
                    resolve(place); // 실패해도 에러 던지지 않고 진행
                    return;
                }
                const coords = response.v2.addresses[0];
                place.coords = new naver.maps.LatLng(coords.y, coords.x);
                resolve(place);
            });
        });
    }

    // 4. 활성 카드 표시
    function highlightActiveCard(id) {
        $$(".place-card").forEach(card => card.classList.remove("active"));
        const activeCard = placeCards[id];
        if (activeCard) {
            activeCard.classList.add("active");
            activeCard.scrollIntoView({ behavior: "smooth", block: "center" });
        }
    }

    // 5. 검색 기능 핸들러 (필터링 기능)
    function handleSearch() {
        const query = searchInput.value.trim().toLowerCase();

        let matchCount = 0;
        let firstMatchId = null;

        PLACES.forEach(place => {
            const card = placeCards[place.id];
            if (!card) return;

            const isMatch = place.name.toLowerCase().includes(query) ||
                            place.address.toLowerCase().includes(query);

            if (isMatch) {
                card.style.display = "block";
                matchCount++;
                if (!firstMatchId) firstMatchId = place.id;
            } else {
                card.style.display = "none";
            }
        });

        if (matchCount === 0) {
            alert('"' + query + '"와(과) 일치하는 장례식장을 찾을 수 없습니다.');
        } else {
            // 첫 번째 결과로 지도 이동 (morph 사용, 줌 레벨 10)
            const marker = markers[firstMatchId];
            if (marker) {
                map.morph(marker.getPosition(), 10);
                highlightActiveCard(firstMatchId);
            }
        }
    }

    // --- 4. 지도 및 데이터 초기화 실행 ---
    async function initMapData() {
        try {
            // 커스텀 마커 아이콘
            const customIcon = {
                url: '/Asset/map_marker.png',
                size: new naver.maps.Size(50, 70),
                scaledSize: new naver.maps.Size(50, 70),
                anchor: new naver.maps.Point(25, 70)
            };

            for (const place of PLACES) {
                try {
                    await geocodeAddress(place);
                    if (place.coords) {
                        // 마커 생성
                        const marker = new naver.maps.Marker({
                            position: place.coords,
                            map: map,
                            title: place.name,
                            icon: customIcon
                        });

                        // 카드 생성 및 추가
                        const card = createPlaceCardElement(place);
                        placeListEl.appendChild(card);

                        // 참조 저장
                        markers[place.id] = marker;
                        placeCards[place.id] = card;

                        // 마커 클릭 이벤트
                        naver.maps.Event.addListener(marker, 'click', function() {
                            infoWindow.setContent(createInfoWindowContent(place));
                            infoWindow.open(map, marker);
                            highlightActiveCard(place.id);
                        });
                    }
                } catch (geoError) {
                    console.warn(`[${place.name}] 처리 중 오류:`, geoError.message);
                }
            }

            // 이벤트 리스너 연결
            if(searchButton) searchButton.addEventListener('click', handleSearch);
            if(searchInput) {
                searchInput.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') handleSearch();
                });
                searchInput.disabled = false;
                searchInput.placeholder = "지역명(예: 대구) 또는 이름 검색";
            }
            if(searchButton) searchButton.disabled = false;

            // 지도 빈 곳 클릭 시 닫기
            naver.maps.Event.addListener(map, 'click', function() {
                infoWindow.close();
                $$(".place-card").forEach(card => card.classList.remove("active"));
            });

            console.log("지도 및 데이터 로딩 완료.");

        } catch (error) {
            console.error("지도 초기화 중 오류 발생:", error);
            if(mapEl) mapEl.innerHTML = "<div style='text-align:center; padding: 20px;'>지도 초기화 중 오류가 발생했습니다.</div>";
            if(searchInput) searchInput.placeholder = "오류 발생";
        }
    }

    // 실행
    initMapData();
});