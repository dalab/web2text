#!/usr/bin/env node

var dir = "/Users/thijs/dev/boilerplate2/src/main/resources/cleaneval/orig/"
var outdir = "../output/unfluff/"
var fs = require('fs')
var extractor = require('unfluff');

fs.readdir(dir, function (err, files) {
  files.forEach(function (file, i) {
    console.log("Looking at",file);
    var fileContents = fs.readFileSync(dir+file,'utf-8');
    var data = extractor(fileContents);
    fs.writeFileSync(outdir+file, data.text,'utf8');
  })
})