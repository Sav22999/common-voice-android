#!/bin/bash

version_checker_file="version-checker.jar"
install_dir="${HOME}/.android/lint"
latest_url=https://api.github.com/repos/PicPay/version-checker-gradle-lint/releases/latest

function request_github() {
    url=$1
    response=$(curl -s $latest_url | grep browser_download_url | sed 's/"//g' | \
     awk '{split($0,a,": "); print a[2]}')
    echo "${response}"
}

function download_jar() {
    output=$1
    url_to_download=$2
    curl -sLo ${output} -H "Accept: application/octet-stream" "${url_to_download}"
}

function install_version_checker_lint() {
    mkdir ${install_dir}
    printf "Installing Version Checker Gradle Lint\n"

    asset_url=$(request_github)

    jar_url=$(request_github "${asset_url}")

    download_jar ${version_checker_file} "${jar_url}"

    mv ${version_checker_file} ${install_dir}

    printf "Version Checker Gradle Lint installed.\n"
}

install_version_checker_lint
