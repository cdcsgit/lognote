# LogNote

Log viewer, Android logcat viewer for Windows, Linux, Mac

Filtered log viewer

Online / offline logcat view

Regular expression filter

Aging Test : Save split file by lines

Kotlin + swing

![Lognote_1 0_main](https://github.com/user-attachments/assets/42707659-de5b-4e81-b6f6-32cdf86e114b)
![Lognote_1 0_open](https://github.com/user-attachments/assets/6868cd9d-a4fc-43c4-9a6e-df48ea208793)


<br/>

# Config path
Save to the path set in the environment variable "LOGNOTE_HOME"\
Default current path

EX) After creating the directory\
Windows : set LOGNOTE_HOME=D:\lognote\
Linux : export LOGNOTE_HOME=\~/lognote\
Mac : export LOGNOTE_HOME=\~/lognote


<br/>

# Run
Windows : start javaw -Dfile.encoding=utf8 -Xmx1024m -jar LogNote.jar\
Linux : java -Dfile.encoding=utf8 -Xmx2048m -jar LogNote.jar\
Mac : java -Dfile.encoding=utf8 -Xmx2048m -jar LogNote.jar

<br/>

# How to use(tips)
1. [Show only specific package logs - 1.0 ~ (use "Packages" menu)](https://cdcsutils.blogspot.com/2024/10/lognote-show-only-specific-package-logs.html)
1. [Show only specific package logs - ~ 0.3.8](https://cdcsutils.blogspot.com/2024/09/lognote-show-only-specific-package-logs.html)
1. [Add desktop shortcut in ubuntu](https://cdcsutils.blogspot.com/2024/10/lognote-setting-up-shortcut-to-re-run.html)
1. [Make screen capture(Add button, Use log trigger)](https://cdcsutils.blogspot.com/2024/12/lognote-make-screen-captureadd-button.html)

<br/>

# Mode
1. Read Cmd: Read the result after executing the command (ex: adb logcat)
   - <code><b>You must set the Scrollback value.</b></code>
     - <b>If the value is not set, logs pile up and occur hang.</b>
     - <b>Recommended - Scrollback: 100000 (approximately 10 Mbytes), enable “Split File” option</b>
1. Read File: Read a file (File > Open, read multiple files continuously)
   - Multiple files : Drag & drop or File > Open files
   - Append files : Ctrl + drag & drop or File > append files
   - Save recent file view config on exit(filters, bookmarks)
   - Open recent files: set to saved view config
1. Follow File: Continue reading logs added to the file (ex: adb logcat > a.log, File > Follow - a.log)
   - Used when you want to read the log of processing results of commands other than adb

<br/>

# Log view mode
1. "Not adb mode(file open...)" or "View > Show process name > None"
![lognote_columnX_processX](https://github.com/user-attachments/assets/9c650931-3c19-4a79-a051-0e7a86d44824)
1. "Not adb mode(file open...)" or "View > Show process name > None" + "View > Show divided by column"
![lognote_columnO_processX](https://github.com/user-attachments/assets/651ef0ea-cc05-46db-b8fe-ced1c525894e)
1. "Adb mode" and "View > Show process name > Show with color bg"
![lognote_columnX_processO](https://github.com/user-attachments/assets/692f8585-0136-41af-a3fe-df3a5df4362a)
1. "Adb mode" and "View > Show process name > Show with color bg" + "View > Show divided by column"
![lognote_columnO_processO](https://github.com/user-attachments/assets/9055ea48-6e7c-4b47-a026-696711b02940)

<br/>

# Shortcut keys
1. Ctrl + B: Toggle Bookmarks, multiple selected lines can be set at the same time
1. Enter: View log dialog (Show long log(with the ends cut off), select string and add to log combo(filter))
1. Ctrl+F: Show find toolbar
    - F3: Move to previous item
    - F4: Move to next item
1. Ctrl + Page Down: Go to end of the log
1. Ctrl + Page Up: Go to the beginning of the log
1. Ctrl + R : stop cmd - connect device - clear log view - start cmd
1. Ctrl + G : Go to line
1. Ctrl + ` : Focus to log combo
1. Ctrl + Del : Clear log view
1. Ctrl + T : Show trigger list panel(aging test util)


<br/>

# Filter combobox color tag
![Lognote_ColorTag](https://user-images.githubusercontent.com/75207513/191993351-396498bc-d5f7-4b92-9a4b-e1b85cb87305.gif)

If enter '#' in the filter combo box, the color tag list is displayed\
![LogNote_ColorTag2](https://github.com/cdcsgit/lognote/assets/75207513/a02cc5bf-1d0a-4527-8a43-2a025f74c2c7)

<br/>

# Filter combobox style
Setting > Filter Style : set style, color
1. Single line
2. Single line - highlight\
![lognote_singleline](https://user-images.githubusercontent.com/75207513/162203519-27a485f4-df90-4f33-8ae5-698957abea49.PNG)
3. Multi line
4. Multi line - highlight\
![lognote_multiline](https://user-images.githubusercontent.com/75207513/162203533-3bf194e8-a093-45a1-a82c-ba1b50bbf118.PNG)

Highlight color : Include text, Exclude text, Separator

<br/>

# Filter combobox size
![lognote_filtercombo_disable](https://user-images.githubusercontent.com/75207513/167983195-848e7aba-123f-44c8-ba7a-944d9923a1a1.gif)

<br/>

# Filter manager
Click : replace\
Ctrl + Click : append\
![Lognote_FilterManager](https://user-images.githubusercontent.com/75207513/191995297-2417f744-4247-4a33-9914-90ca6a758fc3.gif)

<br/>

# Search
Ctrl + F : show search bar\
ESC : hide search bar\
F3 : move to previous\
F4 : move to next\
Click Filter or Full View : Set search target view\
![Lognote_search_bar](https://user-images.githubusercontent.com/75207513/202911181-62787a4e-2bab-4342-a025-695d69cbb5b6.png)

<br/>

# Show process info
When mode is logcat receiving, process info is shown as a tooltip \
Right click > Popup menu > Process info => Show all process list \
![Lognote_processInfo_1](https://github.com/cdcsgit/lognote/assets/75207513/e0baa448-29e7-416c-89ed-f40af4c42e37)
![Lognote_processInfo_2](https://github.com/cdcsgit/lognote/assets/75207513/4f06ee28-e6cf-46d5-a1ec-dd919062aa07)

<br/>

# Log format setting
In addition to logcat logs, you can also use other logs by setting the format \
![Lognote_manage_format](https://github.com/cdcsgit/lognote/assets/75207513/56f6b18d-e56f-4da8-b56a-2a28a27c65d6)


<br/>

# Log trigger - Aging test util(Ctrl-T)
When a specific log occurs, a command is executed or a dialog is displayed \
![lognote_trigger_list](https://github.com/cdcsgit/lognote/assets/75207513/2b551a70-9b75-4ee1-b36f-f4077cc3949e)


<br/>

# Log cmd setting
Set the adb path(to view online log) and Add log cmds \
![lognote_log_cmd_setting](https://user-images.githubusercontent.com/75207513/221393860-31c5efd3-4f5d-4295-b711-d7ec0cd693e1.png)


<br/>

# Color settings
Light(default) \
![lognote_light](https://user-images.githubusercontent.com/75207513/202910342-0a94a05f-9942-41f5-a35f-7fb1a90f8b3e.png)

Dark \
![lognote_dark](https://user-images.githubusercontent.com/75207513/202910351-02db4829-cd77-4e63-bbda-85b501ea7c38.png)

\
Setting > Appearance \
Fixed-width fonts are recommended : The columns for logcat entries(time, pid, tag...) are aligned \
![lognote_appearance_settings](https://user-images.githubusercontent.com/75207513/183441901-de5dbfb4-1b4d-4dca-97a6-cee050d4bd28.png)

<br/>

# View Control
View > Rotation - Rotate 90 degrees clockwise \
View > Full Logs - Toggle show/hide full log view \
Full log view > Windowed Mode - Move the view to new window \
![Lognote_view_ctrl](https://github.com/cdcsgit/lognote/assets/75207513/e376439f-698f-4a48-a5c1-aac0ca406dd6)

<br/>

# Button Icons
![Lognote_Icon](https://user-images.githubusercontent.com/75207513/221393900-f4f0268f-2085-443a-96f8-fcaae8123dbc.gif)

<br/>

# Save split file by lines for aging test
![lognote_split_file](https://user-images.githubusercontent.com/75207513/202910739-e915688d-bf32-4daa-adef-bf2b537b70bc.png) \
Each time 100000 lines are saved, it is changed to a new file

