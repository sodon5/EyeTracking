private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) //해당 쓰레드의 메세지 큐에 메세지가 존재하는 경우 handleMessage() 메소드가 호출됨,
        {
            Toast.makeText(getApplicationContext(), "Call Handler : " + count,LENGTH_SHORT).show();
            //여기다가 추가?

            //chosen_table_num=eye_point_now(leftTop_L);                                                //그냥왼쪽눈기준으로 해놨음 완성시킬 예정   // mTableOver.getId() -> id를 return 해줌
            //set_image_tile(chosen_table_num);

            /*switch( msg.what )
            {
            current_L = mGraphicOverlay.getstoreEyes_L();
            //if(current_L.x!=500) chosen_table_num=13;
            }*/

            if((!isStop))
            {
                mHandler.sendEmptyMessageDelayed(what, 100); // what은 구분자, select 문의 case 처럼  // delay 100 으로 send, 0.1 초에 한번 toast
                count++;
                current_L = mGraphicOverlay.getstoreEyes_L();
                chosen_table_num=eye_point_now(leftTop_L);                                                //그냥왼쪽눈기준으로 해놨음 완성시킬 예정   // mTableOver.getId() -> id를 return 해줌
                set_image_tile(chosen_table_num);
            }
            else
                return;
        }
    };