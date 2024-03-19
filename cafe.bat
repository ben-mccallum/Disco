@echo off

set /p "ds=Enter DS Username: "
ssh -L 3306:devweb2023.cis.strath.ac.uk:3306 %ds%@cafe.cis.strath.ac.uk
