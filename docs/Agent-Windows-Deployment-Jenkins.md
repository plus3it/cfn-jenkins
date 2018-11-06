## About Windows Jenkins Agent

Gotcha: .Net Framework Core 4.7 is a dependency of chocolatey.  The chocolatey installer is not required for every user, and because of this is
in the chain script.  .Net Framework Core must NOT be installed for Visual Studio Build Tools to install properly, so VSBT must be installed BEFORE
.Net Framework Core 4.7.
