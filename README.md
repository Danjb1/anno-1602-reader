# Anno 1602 Sprite Extractor

A tool to extract the graphics from Anno 1602's data files.

![Screenshot](http://www.danjb.com/images/anno/anno_reader.png)

## Compile

From the *src* directory:

    javac main/*.java

## Run

From the *src* directory:

    java main.AnnoReader ANNO_DIR BSH_FILE

Images are saved to an *out* directory.

### Example

    java main.AnnoReader "D:\Anno 1602" GFX/STADTFLD.BSH

## File Guide

### Folders

    GFX      Full zoom graphics
    MGFX     Medium zoom graphics
    SGFX     Small zoom graphics
    TOOLGFX  Interface graphics and more

### GFX

    EFFEKTE      Effects
    FISCHE       Sea life
    GAUKLER      Jugglers
    MAEHER       Farmers
    NUMBERS      Numbers 0-9
    SCHATTEN     Shadows
    SHIP         Ships
    SOLDAT       Soldiers
    STADTFLD     Buildings and terrain
    TIERE        Animals
    TRAEGER      Workers

### TOOLGFX

Each file (except for *SYMBOL*) has two other versions, suffixed by 6 and 8, which correspond to images for small and medium resolutions, respectively.

    BAUHAUS      Building thumbnails and terrain
    BAUSHIP      Ship thumbnails
    EDITOR       Editor interface graphics
    START        Menu graphics
    SYMBOL       Cursor graphics
    TOOLS        Interface graphics

## Credits

A huge thank you to Sir Henry for all his projects, including the Anno 1602 Grafx Tool on which this is based.

Thanks also to the folks in [this thread](http://annozone.com/forum/index.php?page=Thread&threadID=2242).
