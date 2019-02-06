
var columns = document.querySelectorAll("div.columns");
for(var i = 0; i < columns.length ; i++){
    columns[i].classList.add("hidden");
}

var tables = document.querySelectorAll("div.tables");
for(var i = 0; i < tables.length ; i++){
    tables[i].classList.add("hidden");
}

var schemaImg = document.querySelectorAll("img.schemaImg");
for(var i = 0; i < schemaImg.length ; i++){
    schemaImg[i].addEventListener("click", showTables);
}

function showTables(e){
    var schemaName = e.target.id;
    var tables = document.querySelector("div#" + schemaName + "_tables");
    console.log("table = " + tables);
    changeVisability(tables);
}

var tablesImg = document.querySelectorAll("img.tableImg");
for(var i = 0; i < tablesImg.length ; i++){
    tablesImg[i].addEventListener("click", showColumns);
}

function showColumns(e){
    var tableName = e.target.id;
    var columns = document.querySelector("div#" + tableName + "_columns");
    changeVisability(columns);
}

function changeVisability(element){
    if(element.classList.contains("hidden")){
        element.classList.remove("hidden");
        element.classList.add("visible");
    }else{
        element.classList.remove("visible");
        element.classList.add("hidden");
    } 
}