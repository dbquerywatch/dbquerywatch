"use strict";

const fs = require('fs');
const husky = require("husky");

const huskyDir = '.husky';
if (fs.existsSync(huskyDir)) {
    console.log('Husky is already installed.');
} else {
    husky.install();
    husky.add(`${huskyDir}/commit-msg`, 'npx --no-install commitlint --edit "$1"');
}
