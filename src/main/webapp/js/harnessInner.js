//function callJavaMethod() {
var date = Date.now()
var inputElement = document.createElement('input');
inputElement.type = "button"
inputElement.value = 'Upgrade Now'
inputElement.addEventListener('click', function(){
    createPipelineCall(pipeline, date);
});
content = "go-scm sh\n"
content += "go-scm maven\n"
content += "go-scm junit\n"
content += "helloworld sh\n"
content += "helloworld mail\n"
document.getElementById('page-body').style.height = "90%";
document.getElementById('main-panel').style.height = "100%";
document.getElementById('main-panel').innerHTML += "<html><body ><pre style='height:90%;overflow-x:auto;'>" + content + "</pre></body></html>";
document.getElementById('main-panel').appendChild(inputElement);
//
//}