# FastPlace
A multithreaded NMS based chunk updating plugin. Maximum speed I have achieved is updating roughly 8 billion blocks in a second. The server crashes a few minutes later from processing those updated chunks. In order to be fully effective the plugin will need to update the files directly, maybe one day.

# WARNING
The plugin just overrides the chunks with no care. It should only be used if you're willing to overwrite the entire world.