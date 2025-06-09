import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { X } from "lucide-react";
import { menuData } from "../data/MenuItem";

const MobileMenuItem = ({ open, onClose = () => {} }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const menuActiveBg =
    "bg-gradient-to-r from-blue-600 to-95% to-sky-500 text-white  ";

  return (
    <section
      className={` fixed top-0 w-full h-screen duration-700  bg-slate-50/45 z-[999] ${
        open ? "left-0" : "left-[-100%]"
      }`}
      onClick={onClose}
    >
      <article
        className="bg-white w-full max-w-[400px] h-full p-5 flex flex-col justify-start items-start gap-5"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex justify-end w-full cursor-pointer items-center">
          <X size={24} onClick={onClose} />
        </div>
        {menuData.map((menuItem, idx) => {
          return (
            <div
              key={menuItem.key}
              className={`px-2 py-1 cursor-pointer rounded-xl flex  justify-center items-center gap-1 duration-300 transition-all text-start w-[80%]  ${
                location.pathname === menuItem.path && menuActiveBg
              } `}
              onClick={() => {
                onClose();
                setTimeout(() => {
                  navigate(menuItem.path);
                }, 0);
              }}
            >
              <menuItem.icon size={15} />
              {menuItem.label}
            </div>
          );
        })}
      </article>
    </section>
  );
};

export default MobileMenuItem;
