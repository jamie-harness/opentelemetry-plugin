var date = Date.now()
var pipeline = "pipeline:\n  name: go-scm\n  identifier: goscm" + date + "\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        repoName: go-scm\n        build: <+input>\n  stages:\n    - stage:\n        name: go-scm\n        identifier: goscm\n        description: \"\"\n        type: CI\n        spec:\n          cloneCodebase: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - step:\n                  type: Run\n                  name: Vet\n                  identifier: sh_d7\n                  spec:\n                    shell: Sh\n                    command: go vet ./...\n              - stepGroup:\n                  name: Check and Test\n                  identifier: Check_and_Test_91\n                  steps:\n                    - parallel:\n                        - step:\n                            type: Run\n                            name: Check go mod is up to date\n                            identifier: sh_c\n                            spec:\n                              shell: Sh\n                              command: |-\n                                cp go.mod go.mod.bak\n                                go mod tidy\n                                diff go.mod go.mod.bak || (echo 'go.mod is not up to date' && exit 1)\n                        - stepGroup:\n                            name: Test\n                            identifier: Test_1c\n                            steps:\n                              - step:\n                                  type: Run\n                                  name: sh\n                                  identifier: sh_b3\n                                  spec:\n                                    shell: Sh\n                                    command: |-\n                                      go install github.com/jstemmer/go-junit-report/v2@latest\n                                      go test -v 2>&1 -cover ./... | $HOME/go/bin/go-junit-report -set-exit-code > report.xml\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n                              - step:\n                                  type: Run\n                                  name: junit\n                                  identifier: sh_2a\n                                  spec:\n                                    shell: Sh\n                                    command: echo 'This Step is to Upload JUNIT Reports'\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n";
var inputElement = document.createElement('input');
inputElement.type = "button"
inputElement.value = 'Migrate Now'
inputElement.addEventListener('click', function(){
    createPipelineCall(pipeline, date);
});

var body = '<body><h2>Please select pipeline to migrate:</h2><input type="checkbox" class="checkbox" id="selectAllBtn"> Migrate All</input><br><br><div id="checkboxContainer"><label><input type="checkbox" class="checkbox"> Hackathon Pipeline 1</label><br><label><input type="checkbox" class="checkbox"> Sample Pipeline</label><br><label><input type="checkbox" class="checkbox"> Build and Push</label><br><label><input type="checkbox" class="checkbox"> Update Image Version</label><br><label><input type="checkbox" class="checkbox"> go-scm</label><br><br><br></div></body>'
document.getElementById('page-body').style.height = "90%";
document.getElementById('main-panel').style.height = "100%";
//document.getElementById('main-panel').innerHTML += "<html><body ><pre style='height:90%;overflow-x:auto;'>" + pipeline + "</pre></body></html>";
document.getElementById('main-panel').innerHTML += "<html>" + body + "</html>";
document.addEventListener('DOMContentLoaded', function() {
    const selectAllBtn = document.getElementById('selectAllBtn');
    const deselectAllBtn = document.getElementById('deselectAllBtn');
    const checkboxes = document.querySelectorAll('.checkbox');

    selectAllBtn.addEventListener('click', function() {
        checkboxes.forEach(function(checkbox) {
            checkbox.checked = selectAllBtn.checked;
        });
    });
});
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


    document.getElementById('main-panel').innerHTML = '<html lang="en"><head>    <meta charset="UTF-8">    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>Creating Pipeline In Harness</title>    <style>        #progress-container {            width: 100%;            background-color: #f3f3f3;        }        #progress-bar {            width: 0%;            height: 30px;            background-color: #4caf50;        }    </style></head><body>    Upgrade In Progress   <div id="progress-container">        <div id="progress-bar"></div>    </div>    <script src="script.js"></script></body></html>';

    callApisInSequence();

}

function updateProgressBar(progress) {
    progressBar = document.getElementById('progress-bar');
    progressBar.style.width = progress + '%';
}

function callApi(pipeline) {
    var myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/yaml");

    var raw = pipeline
    var requestOptions = {
      method: 'POST',
      headers: myHeaders,
      body: raw,
      redirect: 'follow'
    };

    return fetch("https://app.harness.io/pipeline/api/pipelines/v2?accountIdentifier=9UuUfLwaQ-6ZowvbG7qtLQ&orgIdentifier=default&projectIdentifier=Hackweek_Team_8", requestOptions)
      .then(response => console.log(response.text()))
      .then(result => {
        console.log(result)
        document.getElementById('main-panel').innerHTML = "<h1>Upgrade Successful!</h1>" + link;
      })
      .catch(error => console.log('error', error));
}

function callApisInSequence() {
    const pipelines = [
    'pipeline:\n  name: Hackathon Pipeline 1\n  identifier: Hackathon_Pipeline_1\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        connectorRef: jhttp2\n        build: <+input>\n  stages:\n    - stage:\n        name: Build\n        identifier: Build\n        description: ""\n        type: CI\n        spec:\n          cloneCodebase: true\n          caching:\n            enabled: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - parallel:\n                  - step:\n                      type: Background\n                      name: Run Redis\n                      identifier: Run_Redis\n                      spec:\n                        shell: Sh\n                        command: echo "Running Redis"\n                  - step:\n                      type: Background\n                      name: Run Postgres\n                      identifier: Run_Postgres\n                      spec:\n                        shell: Sh\n                        command: echo "Running Postgres"\n              - step:\n                  type: Test\n                  name: Test Step\n                  identifier: Test_Step\n                  spec:\n                    shell: Sh\n                    command: mvn clean install\n                    intelligenceMode: true\n              - step:\n                  type: BuildAndPushDockerRegistry\n                  name: BuildAndPushDockerRegistry_1\n                  identifier: BuildAndPushDockerRegistry_1\n                  spec:\n                    connectorRef: anuragdocker\n                    repo: anuragharness/jhttp\n                    tags:\n                      - "1"\n                    caching: true\n                    dockerfile: Dockerfile.build\n',
    'pipeline:\n  name: Sample Pipeline\n  identifier: Sample_Pipeline_1\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        connectorRef: jhttp2\n        build: <+input>\n  stages:\n    - stage:\n        name: Build\n        identifier: Build\n        description: ""\n        type: CI\n        spec:\n          cloneCodebase: true\n          caching:\n            enabled: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - parallel:\n                  - step:\n                      type: Background\n                      name: Run Redis\n                      identifier: Run_Redis\n                      spec:\n                        shell: Sh\n                        command: echo "Running Redis"\n                  - step:\n                      type: Background\n                      name: Run Postgres\n                      identifier: Run_Postgres\n                      spec:\n                        shell: Sh\n                        command: echo "Running Postgres"\n              - step:\n                  type: Test\n                  name: Test Step\n                  identifier: Test_Step\n                  spec:\n                    shell: Sh\n                    command: mvn clean install\n                    intelligenceMode: true\n              - step:\n                  type: BuildAndPushDockerRegistry\n                  name: BuildAndPushDockerRegistry_1\n                  identifier: BuildAndPushDockerRegistry_1\n                  spec:\n                    connectorRef: anuragdocker\n                    repo: anuragharness/jhttp\n                    tags:\n                      - "1"\n                    caching: true\n                    dockerfile: Dockerfile.build\n',
    'pipeline:\n  name: Build and Push\n  identifier: Build_and_Push\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        connectorRef: jhttp2\n        build: <+input>\n  stages:\n    - stage:\n        name: Build\n        identifier: Build\n        description: ""\n        type: CI\n        spec:\n          cloneCodebase: true\n          caching:\n            enabled: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - parallel:\n                  - step:\n                      type: Background\n                      name: Run Redis\n                      identifier: Run_Redis\n                      spec:\n                        shell: Sh\n                        command: echo "Running Redis"\n                  - step:\n                      type: Background\n                      name: Run Postgres\n                      identifier: Run_Postgres\n                      spec:\n                        shell: Sh\n                        command: echo "Running Postgres"\n              - step:\n                  type: Test\n                  name: Test Step\n                  identifier: Test_Step\n                  spec:\n                    shell: Sh\n                    command: mvn clean install\n                    intelligenceMode: true\n              - step:\n                  type: BuildAndPushDockerRegistry\n                  name: BuildAndPushDockerRegistry_1\n                  identifier: BuildAndPushDockerRegistry_1\n                  spec:\n                    connectorRef: anuragdocker\n                    repo: anuragharness/jhttp\n                    tags:\n                      - "1"\n                    caching: true\n                    dockerfile: Dockerfile.build\n',
    'pipeline:\n  name: Update Image Version\n  identifier: update_image_version' + 'h3' + '\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        repoName: go-scm\n        build: <+input>\n  stages:\n    - stage:\n        name: go-scm\n        identifier: goscm\n        description: \"\"\n        type: CI\n        spec:\n          cloneCodebase: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - step:\n                  type: Run\n                  name: Vet\n                  identifier: sh_d7\n                  spec:\n                    shell: Sh\n                    command: go vet ./...\n              - stepGroup:\n                  name: Check and Test\n                  identifier: Check_and_Test_91\n                  steps:\n                    - parallel:\n                        - step:\n                            type: Run\n                            name: Check go mod is up to date\n                            identifier: sh_c\n                            spec:\n                              shell: Sh\n                              command: |-\n                                cp go.mod go.mod.bak\n                                go mod tidy\n                                diff go.mod go.mod.bak || (echo "go.mod is not up to date" && exit 1)\n                        - stepGroup:\n                            name: Test\n                            identifier: Test_1c\n                            steps:\n                              - step:\n                                  type: Run\n                                  name: sh\n                                  identifier: sh_b3\n                                  spec:\n                                    shell: Sh\n                                    command: |-\n                                      go install github.com/jstemmer/go-junit-report/v2@latest\n                                      go test -v 2>&1 -cover ./... | $HOME/go/bin/go-junit-report -set-exit-code > report.xml\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n                              - step:\n                                  type: Run\n                                  name: junit\n                                  identifier: sh_2a\n                                  spec:\n                                    shell: Sh\n                                    command: echo "This Step is to Upload JUNIT Reports"\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n',
    'pipeline:\n  name: go-scm\n  identifier: goscmdemo' + 'h4' + '\n  projectIdentifier: Hackweek_Team_8\n  orgIdentifier: default\n  tags: {}\n  properties:\n    ci:\n      codebase:\n        repoName: go-scm\n        build: <+input>\n  stages:\n    - stage:\n        name: go-scm\n        identifier: goscm\n        description: \"\"\n        type: CI\n        spec:\n          cloneCodebase: true\n          platform:\n            os: Linux\n            arch: Amd64\n          runtime:\n            type: Cloud\n            spec: {}\n          execution:\n            steps:\n              - step:\n                  type: Run\n                  name: Vet\n                  identifier: sh_d7\n                  spec:\n                    shell: Sh\n                    command: go vet ./...\n              - stepGroup:\n                  name: Check and Test\n                  identifier: Check_and_Test_91\n                  steps:\n                    - parallel:\n                        - step:\n                            type: Run\n                            name: Check go mod is up to date\n                            identifier: sh_c\n                            spec:\n                              shell: Sh\n                              command: |-\n                                cp go.mod go.mod.bak\n                                go mod tidy\n                                diff go.mod go.mod.bak || (echo "go.mod is not up to date" && exit 1)\n                        - stepGroup:\n                            name: Test\n                            identifier: Test_1c\n                            steps:\n                              - step:\n                                  type: Run\n                                  name: sh\n                                  identifier: sh_b3\n                                  spec:\n                                    shell: Sh\n                                    command: |-\n                                      go install github.com/jstemmer/go-junit-report/v2@latest\n                                      go test -v 2>&1 -cover ./... | $HOME/go/bin/go-junit-report -set-exit-code > report.xml\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n                              - step:\n                                  type: Run\n                                  name: junit\n                                  identifier: sh_2a\n                                  spec:\n                                    shell: Sh\n                                    command: echo "This Step is to Upload JUNIT Reports"\n                                    reports:\n                                      type: JUnit\n                                      spec:\n                                        paths:\n                                          - report.xml\n'
    ];
    let progress = 0;
    const progressIncrement = 100 / pipelines.length;

    var link = '<a href="https://localhost:8181/ng/account/9UuUfLwaQ-6ZowvbG7qtLQ/ci/orgs/default/projects/Hackweek_Team_8/pipelines">Open In Harness</a>';

    pipelines.reduce((promise, pipeline) => {
        return promise.then(() => {
            return callApi(pipeline).then(() => {
                progress += progressIncrement;
                updateProgressBar(progress);
                if (progress === 100) {
                 document.getElementById('main-panel').innerHTML = "<h1>Upgrade Successful!</h1>" + link;
                }
            });
        });
    }, Promise.resolve());
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}