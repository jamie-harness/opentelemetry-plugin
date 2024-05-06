//function callJavaMethod() {
var date = Date.now()
var pipeline = "pipeline:\n  name: go-scm\n  identifier: goscm" + date + "\n  projectIdentifier: anuragworkspace\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        repoName: go-scm\n        build: <+input>\n  stages:\n    - stage:\n        name: go-scm\n        identifier: goscm\n        description: \"\"\n        type: CI\n        spec:\n          cloneCodebase: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - step:\n                  type: Run\n                  name: Vet\n                  identifier: sh_d7\n                  spec:\n                    shell: Sh\n                    command: go vet ./...\n              - stepGroup:\n                  name: Check and Test\n                  identifier: Check_and_Test_91\n                  steps:\n                    - parallel:\n                        - step:\n                            type: Run\n                            name: Check go mod is up to date\n                            identifier: sh_c\n                            spec:\n                              shell: Sh\n                              command: |-\n                                cp go.mod go.mod.bak\n                                go mod tidy\n                                diff go.mod go.mod.bak || (echo 'go.mod is not up to date' && exit 1)\n                        - stepGroup:\n                            name: Test\n                            identifier: Test_1c\n                            steps:\n                              - step:\n                                  type: Run\n                                  name: sh\n                                  identifier: sh_b3\n                                  spec:\n                                    shell: Sh\n                                    command: |-\n                                      go install github.com/jstemmer/go-junit-report/v2@latest\n                                      go test -v 2>&1 -cover ./... | $HOME/go/bin/go-junit-report -set-exit-code > report.xml\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n                              - step:\n                                  type: Run\n                                  name: junit\n                                  identifier: sh_2a\n                                  spec:\n                                    shell: Sh\n                                    command: echo 'This Step is to Upload JUNIT Reports'\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n";
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
    myHeaders.append("x-api-key", "pat.9UuUfLwaQ-6ZowvbG7qtLQ.65b9ca718a04bd7897291d24.OPHt2R07zEalByIktVqf");
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