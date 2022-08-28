# 🍕 개요
## 목적 ?
- 회사에서 업무를 하다보면 일도 중요하지만 하루 중 가장 큰 고민거리는 오늘 뭐먹지? 가 아닐 수 없다
- "짬밥"이 없다면 주변 맛집을 알기에는 다소 어려운 부분이 존재할 것인데 네이버 지도를 이용하여 주변 맛집 정보를 탐색하고 큰 카테고리 내에서 가볼 만한 음식점을 추천해주는 웹을 설계하려고 한다
- 현업에서 자바를 다루는 일이 C#에 비해 부족한 면이 많아 조금이라도 개인적인 공부차원에서 토이 프로젝트를 기획하게 되었다

---

## 📃 Flow
모든 프로젝트에서 가장 중요한 것은 초기 설계단계이다. 이 부분에 대해서는 아직까지 현업에서 설계, 기획을 맡아본적이 없어 나름의 도전이 될수도 있겠지만 노력으로 보강하려고 한다

현재 ERD, USERflow, RestAPI 설계서 를 설계중에 있는데 기본적으로 설계하기에 앞서 참고할 자료가 없기에 실현 가능성이 있는지 확인하기 위해 `테스트`를 먼저 진행하게 되었다

전체적인 틀을 설정하고 간단한 테스트를 통해 가능한지 여부를 판단한다

🏳️ - **1. Enum 을 이용하여 식사메뉴를 설정하고 특정한 카테고리를 선택하고 싶다면 해당 카테고리를, 아니라면 랜덤으로 Enum 내부의 카테고리중 하나를 선정할 수 있도록 한다**
   - Enum 클래스와 랜덤 메소드를 이용하여 카테고리를 특정할 수 있도록하고, 다중 선택또한 가능한지 여부를 확인한다

🏳️ - **2. 서버는 AWS 또는 불가피할 경우 로컬환경에서 돌리되 업데이트 일자를 화면에 표기하여 버튼을 통하여 최신 정보를 스크래핑할 수 있도록 한다**
   - Fiddler, PostMan을 이용하여 어떤 요청과 응답이 오가는지 확인하고 SSL 이슈나 기타 인증서 이슈가 있는지 확인하여 스크래핑 가능여부를 판단한다
   - AWS의 경우 배포경험이 적어 이 부분은 따로 공부를 해가면서 보강해간다

🏳️ - **3. Module화 하여 MSA 로 구성할 수 있는지 여부를 판단한다**
   - 따로 MSA에 대하여 관심이 있어 공부를 시작해볼까 하는데 이기회에 실습을 겸하고자 한다

🏴 - **4. 회원가입의 경우는 OAuth를 사용하여 최대한 사용자에게 간편함을 제공하여 스케쥴러를 통하여 당일이 아닌 이후에 먹을 음식을 탐색하고 스케쥴에 등록할 수 있도록 한다**
   - OAuth 의 경우 카카오톡, 네이버, 구글등이 있는데 카카오톡은 한번도 적용해본적이 없긴하지만 최대한 구성할 수 있도록한다
   - 회원으로 관리하지 않을시 세션에 모든 정보가 담길경우 회원 개인의 달력, 스케쥴로 관리 할 수 없기 때문에 다소 어려움이 존재할 것같다고 판단하였다

🏴 - **5. BE 단에 집중하기 위해 프론트에 대한 시간적 부담을 줄이고자 적당한 템플릿을 선정한다**
   - 무료로 풀리고 있는 템플릿중 도메인과 가장 알맞은 템플릿을 선정하고자 한다 >> 사내 디자이너 분께 템플릿 제공받음 

🏴 - **6. 회원일 경우 카카오 지오코딩/역지오코딩 API를 통하여 좌표 x,y를 받아와 해당 좌표 내에서 boundary를 설정 할 수 있도록 하여 주변 맛집을 검색할 수 있도록한다**
   - 카카오 지도 API에서 주소 정보를 입력받아 x,y좌표로 파싱할 수 있는 방법이 있는지 확인한다
   
>대략적으로 위의 구성대로 Flow를 맞추어 차근차근 준비하고자 합니다.
추가적인 기능들이 추가될수도, 제외될수도 있겠지만 위의 틀에 맞추어 설계하고 그에 맞는 테스트 주도 개발을 경험하는 것이 목표입니다 (🏳️ : 개발준비, 🏴 : 진행완료)
---

<br>
<br>

## ⌨️ 관련 포스팅 일지
<div align="center" style="text-align:center">

  [![Velog's GitHub stats](https://velog-readme-stats.vercel.app/api?name=wjddn3711&color=dark)](https://velog.io/@wjddn3711/%EB%82%98%EC%9D%98-%EC%A3%BC%EB%B3%80-%EB%A7%9B%EC%A7%91%EB%93%A4%EC%9D%84-%EA%B0%84%ED%8E%B8%ED%95%98%EA%B2%8C-%EC%95%8C%EC%95%84%EB%B3%B4%EC%9E%901)
  
</div>



---

<br>
<br>


## 💪 Used Skills
### Platforms & Languages
![Java](https://img.shields.io/badge/Java-007396.svg?&style=for-the-badge&logo=Java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F.svg?&style=for-the-badge&logo=Spring&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E.svg?&style=for-the-badge&logo=JavaScript&logoColor=white)

![HTML5](https://img.shields.io/badge/HTML5-E34F26.svg?&style=for-the-badge&logo=HTML5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6.svg?&style=for-the-badge&logo=CSS3&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1.svg?&style=for-the-badge&logo=MySQL&logoColor=white)

### Tools
![Git](https://img.shields.io/badge/Git-F05032.svg?&style=for-the-badge&logo=Git&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2C2255.svg?&style=for-the-badge&logo=IntelliJ%20IDEA&logoColor=white)

![Postman](https://img.shields.io/badge/Postman-FF6C37.svg?&style=for-the-badge&logo=Postman&logoColor=white)
