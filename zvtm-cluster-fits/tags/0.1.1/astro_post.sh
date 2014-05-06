#!/bin/bash
wget --post-data image="$@" http://127.0.0.1:8000/addImage

