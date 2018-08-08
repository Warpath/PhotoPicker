# PhotoPicker

      private static final int REQ_CHOOSE_IMAGES = 1;

      PhotoPicker.from(MainActivity.this)
                .builder()
                .maxSelectable(9)
                .forResult(REQ_CHOOSE_IMAGES);

      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if (resultCode == Activity.RESULT_OK) {
              if (requestCode == REQ_CHOOSE_IMAGES) {
                  ArrayList<Image> imageList = (ArrayList<Image>) PhotoPicker.obtainResult(data);  
              }
          }
      }
