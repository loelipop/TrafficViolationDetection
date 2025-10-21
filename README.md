# 智慧智慧道路交通違規偵測與自動舉發系統
台灣交通繁忙，機車數量眾多，導致違停、闖紅燈等交通違規行為頻繁發生。僅在2023年1月至8月期間，台灣已發生26萬8330件交通事故，導致1991人死亡（30日內）及35萬9088人受傷。然而，由於現行舉報系統的複雜性，許多違規行為未被有效舉報。基於此問題，本專題旨在開發以台灣臺中市為環境的一款移動設備應用，利用手機的鏡頭做為行車記錄器，並將拍攝的影片即時進行交通違規辨識，並自動鏈接舉報系統，以便快速完成舉報。系統中應用影像辨識技術，精確識別不帶安全帽、闖紅燈、不當變換車道等多種違規行為，並建立自動生成舉發報告的功能，以實現即時通報，從而提升執法效率並減少交通事故的發生。  

關鍵詞：影像辨識、Android App開發、人工智慧  

## App使用技術
支援系統：Android  
使用語言：Java  
資料庫：Firebase Firestore  
身份驗證：Firebase Authentication  

## 功能介紹
**注：本app需要注冊實名帳號，使用者必須擁有中華民國身份證或是居留證方可注冊帳號**  

### 違規偵測
主頁點擊“違規偵測”后，打開手機後鏡頭，點擊“開始/結束錄影”作爲影像的起始與結束，并同時記錄手機的實時經緯度  
<img src="https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E9%8C%84%E5%BD%B1%E5%89%8D.jpg" alt="image_alt" width="400" height="300"><img src="https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E9%8C%84%E5%BD%B1%E4%B8%AD.jpg" alt="image_alt" width="400" height="300">  

結束錄影后影像與經緯度記錄會直接上傳到資料庫，後臺辨識系統即刻開始便是是否有違規車輛  
如有違規車輛，系統將會上傳該車輛資訊與照片至資料庫  
![image_alt](https://github.com/loelipop/TrafficViolationDetection/blob/740721b35104a91e9fc7587b534a7db459e96d57/%E7%B3%BB%E7%B5%B1%E6%9E%B6%E6%A7%8B%E5%9C%96drawio.png)  

### 違規檢舉  
主頁點擊“違規車輛偵測記錄”后，會顯示已被系統偵測的違規車輛列表    
<img src="https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E9%81%95%E8%A6%8F%E7%B4%80%E9%8C%84-%E5%B7%B2%E6%AA%A2%E8%88%89%E5%88%97%E8%A1%A8.jpg" alt="image_alt" width="300" height="400">

點擊想要檢舉的車輛可以查看具體違規事項和地點，若判斷想要檢舉可點擊“檢舉”即可跳轉到臺中市交通違規檢舉網頁，若不想則點擊“刪除”即可刪除記錄  
![image_alt](https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E9%81%95%E8%A6%8F%E8%BB%8A%E8%BC%9B%E5%81%B5%E6%B8%AC%E7%B4%80%E9%8C%84-%E6%9C%AA%E6%AA%A2%E8%88%89%E7%9A%84%E7%95%AB%E9%9D%A2.jpg)  

打開臺中市交通違規檢舉網頁后，系統會自動填寫已儲存在資料庫的檢舉人資訊以及違規車輛資訊，使用者只需檢查資料的正確性並手動點擊網頁上的檢舉按鈕即可完成檢舉
![image_alt](https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E6%AA%A2%E8%88%89%E7%B6%B2%E9%A0%811.jpg)  ![image_alt](https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E6%AA%A2%E8%88%89%E7%B6%B2%E9%A0%812.jpg)  ![image_alt](https://github.com/loelipop/TrafficViolationDetection/blob/8089516565021632add7f0ce1f540d3ddf5b33a3/%E6%AA%A2%E8%88%89%E7%B6%B2%E9%A0%813.jpg)  
