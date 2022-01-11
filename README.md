# LogNote

Android logcat viewer for Windows, Linux

kotlin + swing

Regular expression filter

Online / offline log view

Save split file by lines for aging test

# Color settings
Light(default)
![default_color](https://user-images.githubusercontent.com/75207513/148026944-d965a90e-f2e4-478d-a763-f9d229d36f4c.png)

Dark
![dark_color](https://user-images.githubusercontent.com/75207513/148026947-e713661d-a876-41c6-99c3-877596c098ad.png)
Add or change color configuration in lognote.xml as below.
```xml
<entry key="COLOR_MANAGER_0">#F05050</entry>
<entry key="COLOR_MANAGER_1">#101010</entry>
<entry key="COLOR_MANAGER_2">#2B2B2B</entry>
<entry key="COLOR_MANAGER_3">#2B2B2B</entry>
<entry key="COLOR_MANAGER_4">#353535</entry>
<entry key="COLOR_MANAGER_5">#503030</entry>
<entry key="COLOR_MANAGER_6">#F0F0F0</entry>
<entry key="COLOR_MANAGER_7">#F0F0F0</entry>
<entry key="COLOR_MANAGER_8">#6C9876</entry>
<entry key="COLOR_MANAGER_9">#5084C4</entry>
<entry key="COLOR_MANAGER_10">#CB8742</entry>
<entry key="COLOR_MANAGER_11">#CD6C79</entry>
<entry key="COLOR_MANAGER_12">#ED3030</entry>
<entry key="COLOR_MANAGER_13">#A0A0F0</entry>
<entry key="COLOR_MANAGER_14">#A0A0F0</entry>
<entry key="COLOR_MANAGER_15">#A0A0F0</entry>
<entry key="COLOR_MANAGER_16">#A0A0F0</entry>
<entry key="COLOR_MANAGER_17">#F0F0F0</entry>
```

# Run
windows : start javaw -Xmx4096m -jar LogNote.jar\
linux : java -Xmx4096m -jar LogNote.jar
