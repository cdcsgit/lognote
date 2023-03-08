# LogNote

Log viewer, Android logcat viewer for Windows, Linux, Mac

Filtered log viewer

Online / offline logcat view

Regular expression filter

Aging Test : Save split file by lines

Kotlin + swing

![lognote_light](https://user-images.githubusercontent.com/75207513/202910342-0a94a05f-9942-41f5-a35f-7fb1a90f8b3e.png)
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

# Log cmd setting
Set the adb path(to view online log) and Add log cmds \
![lognote_log_cmd_setting](https://user-images.githubusercontent.com/75207513/221393860-31c5efd3-4f5d-4295-b711-d7ec0cd693e1.png)

<br/>


# Filter combobox color tag
![Lognote_ColorTag](https://user-images.githubusercontent.com/75207513/191993351-396498bc-d5f7-4b92-9a4b-e1b85cb87305.gif)

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

# Color settings
Light(default) \
![lognote_light](https://user-images.githubusercontent.com/75207513/202910342-0a94a05f-9942-41f5-a35f-7fb1a90f8b3e.png)

Dark \
![lognote_dark](https://user-images.githubusercontent.com/75207513/202910351-02db4829-cd77-4e63-bbda-85b501ea7c38.png)

\
Setting > Appearance \
![lognote_appearance_settings](https://user-images.githubusercontent.com/75207513/183441901-de5dbfb4-1b4d-4dca-97a6-cee050d4bd28.png)

<br/>

# Button Icons
![Lognote_Icon](https://user-images.githubusercontent.com/75207513/221393900-f4f0268f-2085-443a-96f8-fcaae8123dbc.gif)

<br/>

# Save split file by lines for aging test
![lognote_split_file](https://user-images.githubusercontent.com/75207513/202910739-e915688d-bf32-4daa-adef-bf2b537b70bc.png) \
Each time 100000 lines are saved, it is changed to a new file

