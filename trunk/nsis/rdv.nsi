!include "MUI.nsh"

Name "RDV"
Caption "RDV Setup"
OutFile "..\build\exe\rdv-${version}-setup.exe"
InstallDir $PROGRAMFILES\RDV
InstallDirRegKey HKLM "Software\RDV" ""

Var MUI_TEMP
Var STARTMENU_FOLDER

!define MUI_ABORTWARNING

!define MUI_WELCOMEPAGE_TITLE "Welcome to the RDV Setup Wizard"
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of RDV.\r\n\r\n$_CLICK"

!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\RDV" 
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"

!define MUI_FINISHPAGE_RUN "$INSTDIR\RDV.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Run RDV"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

RequestExecutionLevel admin

Section "Install"

  SetOutPath $INSTDIR
  File "..\build\exe\RDV.exe"
  File "..\*.txt"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM "SOFTWARE\RDV" "" "$INSTDIR"
  
  ; Cleanup old start menu items
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
  Delete "$SMPROGRAMS\$MUI_TEMP\RDV.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall RDV.lnk"
  RMDir "$SMPROGRAMS\$MUI_TEMP"
  
  SetShellVarContext all
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application   
  CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\RDV.lnk" "$INSTDIR\RDV.exe"
  CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall RDV.lnk" "$INSTDIR\uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RDV" "DisplayName" "RDV"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RDV" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RDV" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RDV" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
  ; Cleanup old keys
  DeleteRegKey HKLM "Software\NEES\RDV"
  DeleteRegKey /ifempty HKLM "Software\NEES"
  DeleteRegKey HKCU "Software\NEES\RDV"
  DeleteRegKey /ifempty HKCU "Software\NEES"

SectionEnd

Section "Uninstall"
  
  Delete "$INSTDIR\RDV.exe"
  Delete "$INSTDIR\*.txt"
  Delete "$INSTDIR\uninstall.exe"
  RMDir "$INSTDIR"

  SetShellVarContext all
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
  Delete "$SMPROGRAMS\$MUI_TEMP\RDV.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall RDV.lnk"
  RMDir "$SMPROGRAMS\$MUI_TEMP"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RDV"
  DeleteRegKey HKLM "Software\RDV"

SectionEnd