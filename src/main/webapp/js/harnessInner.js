//function callJavaMethod() {
var date = Date.now()
var inputElement = document.createElement('input');
inputElement.type = "button"
inputElement.value = 'Upgrade Now'
inputElement.addEventListener('click', function(){
    createPipelineCall(pipeline, date);
});
document.getElementById('page-body').style.height = "90%";
document.getElementById('main-panel').style.height = "100%";
document.getElementById('main-panel').innerHTML += "<html><body ><pre style='height:90%;overflow-x:auto;'>" + pipeline + "</pre></body></html>";
document.getElementById('main-panel').appendChild(inputElement);
//
//}

function callJavaMethod2() {
    var res = myAction.getCallUpgrade2();
    alert(res);
    document.getElementById('main-panel').innerHTML = "<html><body><pre>" + res + "</pre></body></html>";
}

function callJavaMethod3() {
    var res = myAction.getCallUpgrade3();
    alert(res);
    document.getElementById('main-panel').innerHTML = "<html><body><pre>" + res + "</pre></body></html>";
}

function createPipelineCall(pipeline, date) {
    var myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/yaml");
    var link = '<a href="https://app.harness.io/ng/account/9UuUfLwaQ-6ZowvbG7qtLQ/ci/orgs/default/projects/anuragworkspace/pipelines/goscm' + date + '/pipeline-studio/?storeType=INLINE">Open In Harness</a>';

    var raw = pipeline
    var requestOptions = {
      method: 'POST',
      headers: myHeaders,
      body: raw,
      redirect: 'follow'
    };

    fetch("https://app.harness.io/pipeline/api/pipelines/v2?accountIdentifier=9UuUfLwaQ-6ZowvbG7qtLQ&orgIdentifier=default&projectIdentifier=anuragworkspace", requestOptions)
      .then(response => console.log(response.text()))
      .then(result => {
        console.log(result)
        document.getElementById('main-panel').innerHTML = "<h1>Upgrade Successful!</h1>" + link + "<html><body><pre style='height:80%;overflow-x:auto;'>" + pipeline + "</pre></body></html>";
      })
      .catch(error => console.log('error', error));
}