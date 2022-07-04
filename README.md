# LogNote

Android logcat viewer for Windows, Linux, Mac

Kotlin + swing

Regular expression filter

Online / offline log view

Aging Test : Save split file by lines

<br/>

# Run
Windows : start javaw -Dfile.encoding=utf8 -Xmx1024m -jar LogNote.jar\
Linux : java -Dfile.encoding=utf8 -Xmx2048m -jar LogNote.jar\
Mac : java -Dfile.encoding=utf8 -Xmx2048m -jar LogNote.jar

<br/>

# Config path
Save to the path set in the environment variable "LOGNOTE_HOME"\
Default current path

<br/>

# Adb setting
Set the adb path(to view online log) \
![lognote_adb_setting](https://user-images.githubusercontent.com/75207513/166401425-faaa46a9-da64-44df-bfd4-21733b694929.png)

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

# Color settings
Light(default) \
<img src="https://user-images.githubusercontent.com/75207513/148026944-d965a90e-f2e4-478d-a763-f9d229d36f4c.png" width="600">

Dark \
<img src="https://user-images.githubusercontent.com/75207513/148026947-e713661d-a876-41c6-99c3-877596c098ad.png" width="600">

\
Setting > Font & Color \
<img src="https://user-images.githubusercontent.com/75207513/160410523-afcb82c2-78de-4695-a372-ac7d32533464.png" width="300">

<br/>

# Save split file by lines for aging test
![aging](https://user-images.githubusercontent.com/75207513/150263408-d64b7003-6b9c-460f-a4e6-02e6a4ee01e9.png) \
Each time 100000 lines are saved, it is changed to a new file
